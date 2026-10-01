package com.example.emailscannerservice.service;

import com.example.emailscannerservice.domain.PerfilSenhaUsuario;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gera candidatos de senha a partir de convenções PÚBLICAS usadas por
 * bancos brasileiros para PDFs de boleto (não é força bruta genérica —
 * é um "chute inteligente" baseado em padrão conhecido). Isso só roda se
 * o usuário cadastrou CPF/data de nascimento por opção própria; sem isso,
 * a lista vem vazia e o boleto cai direto como BLOQUEADO.
 */
@Service
public class SenhaPatternService {

    public List<String> gerarCandidatos(PerfilSenhaUsuario perfil) {
        List<String> candidatos = new ArrayList<>();
        if (perfil == null) {
            return candidatos;
        }

        String cpf = perfil.getCpf() != null ? perfil.getCpf().replaceAll("[^0-9]", "") : null;
        var nascimento = perfil.getDataNascimento();

        if (cpf != null && cpf.length() == 11) {
            candidatos.add(cpf.substring(0, 4));   // padrão comum: 4 primeiros dígitos do CPF
            candidatos.add(cpf.substring(0, 3));   // variação: 3 primeiros
            candidatos.add(cpf);                   // variação: CPF completo
        }

        if (nascimento != null) {
            candidatos.add(nascimento.format(DateTimeFormatter.ofPattern("ddMMyyyy")));
            candidatos.add(nascimento.format(DateTimeFormatter.ofPattern("ddMM")));
            candidatos.add(nascimento.format(DateTimeFormatter.ofPattern("MMyyyy")));
        }

        return candidatos;
    }
}
