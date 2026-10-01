package com.example.emailscannerservice.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abre o PDF (tentando as senhas candidatas se estiver criptografado) e
 * usa expressões regulares pra achar os campos do boleto no texto extraído.
 *
 * IMPORTANTE (limite honesto): isso é parsing por regex sobre o texto do
 * PDF, não OCR nem um parser oficial de boleto — funciona bem pra PDFs
 * "nativos" (texto selecionável) com o layout padrão Febraban, mas pode
 * falhar em boletos escaneados como imagem ou em layouts muito fora do
 * padrão. Nesses casos o resultado cai como ERRO_EXTRACAO.
 */
@Service
public class PdfBoletoExtractorService {

    private static final Logger log = LoggerFactory.getLogger(PdfBoletoExtractorService.class);

    private static final Pattern LINHA_DIGITAVEL =
            Pattern.compile("(\\d{5}[.\\s]?\\d{5}\\s?\\d{5}[.\\s]?\\d{6}\\s?\\d{5}[.\\s]?\\d{6}\\s?\\d\\s?\\d{14})");
    private static final Pattern VALOR = Pattern.compile("R\\$\\s?([\\d.,]+)");
    private static final Pattern VENCIMENTO = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern DOCUMENTO = Pattern.compile("(\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})");

    public record ResultadoExtracao(
            boolean sucesso,
            boolean precisaSenha,
            String linhaDigitavel,
            String beneficiarioNome,
            String beneficiarioDocumento,
            BigDecimal valor,
            LocalDate vencimento
    ) {
        static ResultadoExtracao aguardandoSenha() {
            return new ResultadoExtracao(false, true, null, null, null, null, null);
        }
        static ResultadoExtracao falha() {
            return new ResultadoExtracao(false, false, null, null, null, null, null);
        }
    }

    public ResultadoExtracao extrair(byte[] pdfBytes, List<String> senhasCandidatas) {
        // 1a tentativa: sem senha (a maioria dos boletos não é protegida)
        Optional<String> texto = tentarAbrirEExtrairTexto(pdfBytes, "");
        if (texto.isPresent()) {
            return parsearTexto(texto.get());
        }

        // 2a tentativa: padrões conhecidos de senha bancária
        for (String senha : senhasCandidatas) {
            texto = tentarAbrirEExtrairTexto(pdfBytes, senha);
            if (texto.isPresent()) {
                return parsearTexto(texto.get());
            }
        }

        return ResultadoExtracao.aguardandoSenha();
    }

    /** Usado no fluxo de desbloqueio manual, quando o usuário digita a senha. */
    public ResultadoExtracao extrairComSenhaManual(byte[] pdfBytes, String senha) {
        return tentarAbrirEExtrairTexto(pdfBytes, senha)
                .map(this::parsearTexto)
                .orElse(ResultadoExtracao.falha());
    }

    private Optional<String> tentarAbrirEExtrairTexto(byte[] pdfBytes, String senha) {
        try (PDDocument documento = Loader.loadPDF(pdfBytes, senha)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return Optional.of(stripper.getText(documento));
        } catch (IOException e) {
            // senha incorreta ou PDF corrompido — tentativa seguinte decide o que fazer
            return Optional.empty();
        }
    }

    private ResultadoExtracao parsearTexto(String texto) {
        try {
            String linhaDigitavel = buscar(LINHA_DIGITAVEL, texto).map(s -> s.replaceAll("[.\\s]", "")).orElse(null);
            String documento = buscar(DOCUMENTO, texto).orElse(null);
            BigDecimal valor = buscar(VALOR, texto)
                    .map(v -> v.replace(".", "").replace(",", "."))
                    .map(BigDecimal::new)
                    .orElse(null);
            LocalDate vencimento = buscar(VENCIMENTO, texto)
                    .map(v -> LocalDate.parse(v, DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .orElse(null);

            if (linhaDigitavel == null || valor == null || vencimento == null) {
                return ResultadoExtracao.falha();
            }

            return new ResultadoExtracao(true, false, linhaDigitavel, extrairNomeBeneficiario(texto), documento, valor, vencimento);

        } catch (Exception e) {
            log.warn("Falha ao interpretar texto extraído do PDF: {}", e.getMessage());
            return ResultadoExtracao.falha();
        }
    }

    private Optional<String> buscar(Pattern padrao, String texto) {
        Matcher matcher = padrao.matcher(texto);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private String extrairNomeBeneficiario(String texto) {
        // Heurística simples: primeira linha não vazia do texto costuma ser
        // o cabeçalho com o nome do beneficiário na maioria dos layouts
        // Febraban. Não é garantido — por isso o usuário sempre revisa os
        // dados na tela antes de confirmar a análise.
        return texto.lines()
                .map(String::trim)
                .filter(linha -> !linha.isBlank())
                .findFirst()
                .orElse("Beneficiário não identificado");
    }
}
