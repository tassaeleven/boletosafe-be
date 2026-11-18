package com.example.emailscannerservice.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Fala com a Gmail API usando o access token que o Spring Security guarda
 * depois do login OAuth2 do usuário com o Google.
 */
@Service
public class GmailScannerService {

    private static final Logger log = LoggerFactory.getLogger(GmailScannerService.class);

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
            log.info("Gmail não retornou mensagens para a query '{}'", QUERY_BOLETOS);
            return encontrados;
        }
        log.info("Gmail retornou {} mensagens", listagem.getMessages().size());

        for (var resumo : listagem.getMessages()) {
            Message mensagem = gmail.users().messages().get("me", resumo.getId()).execute();
            String remetente = extrairHeader(mensagem, "From");
            String assunto = extrairHeader(mensagem, "Subject");

            for (MessagePart parte : partesComAnexo(mensagem.getPayload())) {
                boolean pdf = "application/pdf".equalsIgnoreCase(parte.getMimeType())
                        || (parte.getFilename() != null && parte.getFilename().toLowerCase().endsWith(".pdf"));
                if (!pdf) {
                    log.info("Ignorando anexo '{}' ({})", parte.getFilename(), parte.getMimeType());
                    continue;
                }

                byte[] bytes = baixarAnexo(gmail, mensagem.getId(), parte.getBody().getAttachmentId());
                encontrados.add(new AnexoEncontrado(mensagem.getId(), remetente, assunto,
                        parte.getFilename(), bytes));
            }
        }

        return encontrados;
    }

    private Gmail construirClienteGmail(OAuth2AuthenticationToken authentication)
            throws GeneralSecurityException, IOException {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        OAuth2AccessToken accessToken = client.getAccessToken();
        HttpTransport transporte = GoogleNetHttpTransport.newTrustedTransport();

        return new Gmail.Builder(transporte, GsonFactory.getDefaultInstance(),
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken.getTokenValue()))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private List<MessagePart> partesComAnexo(MessagePart raiz) {
        List<MessagePart> partes = new ArrayList<>();
        if (raiz == null) {
            return partes;
        }
        if (raiz.getFilename() != null && !raiz.getFilename().isBlank()
                && raiz.getBody() != null && raiz.getBody().getAttachmentId() != null) {
            partes.add(raiz);
        }
        if (raiz.getParts() != null) {
            for (MessagePart filha : raiz.getParts()) {
                partes.addAll(partesComAnexo(filha));
            }
        }
        return partes;
    }

    private byte[] baixarAnexo(Gmail gmail, String mensagemId, String attachmentId) throws IOException {
        var anexo = gmail.users().messages().attachments().get("me", mensagemId, attachmentId).execute();
        return Base64.getUrlDecoder().decode(anexo.getData());
    }

    private String extrairHeader(Message mensagem, String nome) {
        if (mensagem.getPayload() == null || mensagem.getPayload().getHeaders() == null) {
            return null;
        }
        return mensagem.getPayload().getHeaders().stream()
                .filter(h -> nome.equalsIgnoreCase(h.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse(null);
    }
}