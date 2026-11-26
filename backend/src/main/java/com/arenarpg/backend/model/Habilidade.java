package com.arenarpg.backend.model;

public final class Habilidade {
    private final String nome;
    private final String descricao;
    private final int dano;
    private final int custoMana;
    private final int alcance;
    private final TipoHabilidade tipo;

    public Habilidade(
            String nome,
            String descricao,
            int dano,
            int custoMana,
            int alcance,
            TipoHabilidade tipo) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da habilidade é obrigatório.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição da habilidade é obrigatória.");
        }
        if (dano < 0 || custoMana < 0 || alcance < 1) {
            throw new IllegalArgumentException("Dano e custo não podem ser negativos; alcance deve ser positivo.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da habilidade é obrigatório.");
        }

        this.nome = nome.trim();
        this.descricao = descricao.trim();
        this.dano = dano;
        this.custoMana = custoMana;
        this.alcance = alcance;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getDano() {
        return dano;
    }

    public int getCustoMana() {
        return custoMana;
    }

    public int getAlcance() {
        return alcance;
    }

    public TipoHabilidade getTipo() {
        return tipo;
    }
}
