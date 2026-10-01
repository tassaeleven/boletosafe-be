package com.example.emailscannerservice.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Fala com a Gmail API usando o access token que o Spring Security guarda
 * depois do login OAuth2 do usuário com o Google (fluxo padrão
 * spring-boot-starter-oauth2-client, configurado em application.yml).
 *
 * Pré-requisito fora do código: criar um projeto no Google Cloud Console,
 * ativar a Gmail API e configurar um OAuth Client ID (tipo Web) com o
 * scope "https://www.googleapis.com/auth/gmail.readonly".
 */
@Service
public class GmailScannerService {

    private static final String APPLICATION_NAME = "BoletoSafe Email Scanner";
    private static final String QUERY_BOLETOS = "has:attachment filename:pdf newer_than:30d";

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GmailScannerService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    public record AnexoEncontrado(String mensagemId, String remetente, String assunto,
                                   String nomeArquivo, byte[] conteudo) {}

    public List<AnexoEncontrado> buscarBoletosRecentes(OAuth2AuthenticationToken authentication) throws Exception {
        Gmail gmail = construirClienteGmail(authentication);
        List<AnexoEncontrado> encontrados = new ArrayList<>();

        var listagem = gmail.users().messages().list("me").setQ(QUERY_BOLETOS).execute();
        if (listagem.getMessages() == null) {
            return encontrados;
        }

        for (var resumo : listagem.getMessages()) {
            Message mensagem = gmail.users().messages().get("me", resumo.getId()).execute();
            String remetente = extrairHeader(mensagem, "From");
            String assunto = extrairHeader(mensagem, "Subject");

            for (MessagePart parte : partesComAnexo(mensagem)) {
                if (!"application/pdf".equalsIgnoreCase(parte.getMimeType())) continue;

                byte[] bytes = baixarAnexo(gmail, mensagem.getId(), parte.getBody().getAttachmentId());
                encontrados.add(new AnexoEncontrado(mensagem.getId(), remetente, assunto,
                        parte.getFilename(), bytes));
            }
        }

        return encontrados;
    }

    private Gmail construirClienteGmail(OAuth2AuthenticationToken authentication) throws GeneralSecurityException, java.io.IOException {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        OAuth2AccessToken accessToken = client.getAccessToken();
        HttpTransport transporte = GoogleNetHttpTransport.newTrustedTransport();

        return new Gmail.Builder(transporte, GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue()))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private List<MessagePart> partesComAnexo(Message mensagem) {
        List<MessagePart> partes = new ArrayList<>();
        if (mensagem.getPayload() == null || mensagem.getPayload().getParts() == null) {
            return partes;
        }
        for (MessagePart parte : mensagem.getPayload().getParts()) {
            if (parte.getFilename() != null && !parte.getFilename().isBlank()
                    && parte.getBody() != null && parte.getBody().getAttachmentId() != null) {
                partes.add(parte);
            }
        }
        return partes;
    }

    private byte[] baixarAnexo(Gmail gmail, String mensagemId, String attachmentId) throws java.io.IOException {
        var anexo = gmail.users().messages().attachments().get("me", mensagemId, attachmentId).execute();
        return Base64.getUrlDecoder().decode(anexo.getData());
    }

    private String extrairHeader(Message mensagem, String nome) {
        if (mensagem.getPayload() == null || mensagem.getPayload().getHeaders() == null) return null;
        return mensagem.getPayload().getHeaders().stream()
                .filter(h -> nome.equalsIgnoreCase(h.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse(null);
    }
}
