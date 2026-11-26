package com.arenarpg.backend.dto;

import java.util.Locale;

import com.arenarpg.backend.model.Habilidade;

public record HabilidadeDTO(
        String nome,
        String descricao,
        int dano,
        int custoMana,
        int alcance,
        String tipo) {

    public static HabilidadeDTO de(Habilidade habilidade) {
        return new HabilidadeDTO(
                habilidade.getNome(),
                habilidade.getDescricao(),
                habilidade.getDano(),
                habilidade.getCustoMana(),
                habilidade.getAlcance(),
                habilidade.getTipo().name().toLowerCase(Locale.ROOT));
    }
}
