package com.example.boletoriskengine.config;

import com.example.boletoriskengine.domain.BancoInstituicao;
import com.example.boletoriskengine.domain.OrigemCobranca;
import com.example.boletoriskengine.domain.RegraRisco;
import com.example.boletoriskengine.repository.BancoInstituicaoRepository;
import com.example.boletoriskengine.repository.OrigemCobrancaRepository;
import com.example.boletoriskengine.repository.RegraRiscoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Popula as 10 regras de risco (com os pesos definidos com o professor
 * e o grupo) e alguns bancos/origens de cobranca basicos, so em ambiente
 * de desenvolvimento. Em producao isso vira uma migration versionada
 * (Flyway/Liquibase).
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final RegraRiscoRepository regraRiscoRepository;
    private final BancoInstituicaoRepository bancoInstituicaoRepository;
    private final OrigemCobrancaRepository origemCobrancaRepository;

    public DataSeeder(RegraRiscoRepository regraRiscoRepository,
                       BancoInstituicaoRepository bancoInstituicaoRepository,
                       OrigemCobrancaRepository origemCobrancaRepository) {
        this.regraRiscoRepository = regraRiscoRepository;
        this.bancoInstituicaoRepository = bancoInstituicaoRepository;
        this.origemCobrancaRepository = origemCobrancaRepository;
    }

    @Override
    public void run(String... args) {
        seedRegras();
        seedBancos();
        seedOrigens();
    }

    private void seedRegras() {
        if (regraRiscoRepository.count() > 0) return;

        criarRegra("DIGITO_VERIFICADOR_INVALIDO", "Dígito verificador inválido",
                "Valida o DV de cada campo da linha digitável", "ESTRUTURAL", 30);
        criarRegra("BANCO_EMISSOR_INVALIDO", "Banco emissor inválido",
                "Código de banco não corresponde a instituição conhecida", "ESTRUTURAL", 25);
        criarRegra("DOCUMENTO_BENEFICIARIO_INVALIDO", "Documento do beneficiário inválido",
                "CPF/CNPJ com dígito verificador incorreto", "BENEFICIARIO", 20);
        criarRegra("BENEFICIARIO_NOVO", "Beneficiário novo",
                "Primeira cobrança registrada para este beneficiário", "BENEFICIARIO", 5);
        criarRegra("MUDANCA_DADOS_BANCARIOS", "Mudança de dados bancários",
                "Banco emissor mudou desde a última cobrança do beneficiário", "BENEFICIARIO", 40);
        criarRegra("NOME_BENEFICIARIO_SIMILAR_SUSPEITO", "Nome de beneficiário parecido/suspeito",
                "Nome muito parecido com outro beneficiário já conhecido", "BENEFICIARIO", 25);
        criarRegra("VALOR_FORA_DA_MEDIA_HISTORICA", "Valor fora da média histórica",
                "Valor mais que o dobro da média de cobranças do beneficiário", "VALOR_VENCIMENTO", 15);
        criarRegra("VENCIMENTO_PROXIMO_DEMAIS", "Vencimento próximo demais",
                "Vencimento igual a hoje ou já vencido", "VALOR_VENCIMENTO", 10);
        criarRegra("ORIGEM_COBRANCA_SUSPEITA", "Origem de cobrança suspeita",
                "Cobrança recebida por canal informal (e-mail/WhatsApp/SMS)", "ORIGEM", 10);
        criarRegra("REINCIDENCIA_ALERTA_ALTO", "Reincidência de alerta alto",
                "Beneficiário já esteve envolvido em alerta de risco alto", "REINCIDENCIA", 20);
    }

    private void criarRegra(String codigo, String nome, String descricao, String categoria, int peso) {
        RegraRisco regra = new RegraRisco();
        regra.setCodigo(codigo);
        regra.setNome(nome);
        regra.setDescricao(descricao);
        regra.setCategoria(categoria);
        regra.setPesoPadrao(peso);
        regra.setAtiva(true);
        regraRiscoRepository.save(regra);
    }

    private void seedBancos() {
        if (bancoInstituicaoRepository.count() > 0) return;

        salvarBanco("001", "Banco do Brasil");
        salvarBanco("104", "Caixa Econômica Federal");
        salvarBanco("237", "Bradesco");
        salvarBanco("341", "Itaú Unibanco");
        salvarBanco("033", "Santander");
    }

    private void salvarBanco(String codigo, String nome) {
        BancoInstituicao banco = new BancoInstituicao();
        banco.setCodigoFebraban(codigo);
        banco.setNome(nome);
        bancoInstituicaoRepository.save(banco);
    }

    private void seedOrigens() {
        if (origemCobrancaRepository.count() > 0) return;

        salvarOrigem("PORTAL", "Portal oficial do fornecedor");
        salvarOrigem("CARTA", "Carta física");
        salvarOrigem("EMAIL", "E-mail");
        salvarOrigem("WHATSAPP", "WhatsApp");
        salvarOrigem("SMS", "SMS");
    }

    private void salvarOrigem(String codigo, String descricao) {
        OrigemCobranca origem = new OrigemCobranca();
        origem.setCodigo(codigo);
        origem.setDescricao(descricao);
        origemCobrancaRepository.save(origem);
    }
}
