package com.arenarpg.backend.dto;

import java.util.List;

import com.arenarpg.backend.model.Personagem;

public record PersonagemDTO(
        String tipo,
        String nome,
        int vidaMaxima,
        int vidaAtual,
        int ataque,
        int defesa,
        int manaMaxima,
        int manaAtual,
        int alcance,
        int movimentoMaximo,
        List<HabilidadeDTO> habilidades) {

    public PersonagemDTO {
        habilidades = List.copyOf(habilidades);
    }

    public static PersonagemDTO de(Personagem personagem) {
        List<HabilidadeDTO> habilidades = personagem.getHabilidadeEspecial()
                .map(HabilidadeDTO::de)
                .map(List::of)
                .orElseGet(List::of);

        return new PersonagemDTO(
                personagem.getTipo().getIdentificador(),
                personagem.getNome(),
                personagem.getVidaMaxima(),
                personagem.getVidaAtual(),
                personagem.getAtaque(),
                personagem.getDefesa(),
                personagem.getManaMaxima(),
                personagem.getManaAtual(),
                personagem.getAlcance(),
                personagem.getMovimentoMaximo(),
                habilidades);
    }
}
