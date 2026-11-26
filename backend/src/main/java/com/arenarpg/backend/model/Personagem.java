package com.arenarpg.backend.model;

import java.util.Optional;

public abstract class Personagem {
    private final TipoPersonagem tipo;
    private final String nome;
    private final int vidaMaxima;
    private int vidaAtual;
    private final int ataque;
    private final int defesa;
    private final int manaMaxima;
    private int manaAtual;
    private final int alcance;
    private final int movimentoMaximo;
    private final Habilidade habilidadeEspecial;

    protected Personagem(
            TipoPersonagem tipo,
            String nome,
            int vidaMaxima,
            int ataque,
            int defesa,
            int manaMaxima,
            int alcance,
            int movimentoMaximo,
            Habilidade habilidadeEspecial) {
        if (tipo == null || nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Tipo e nome do personagem são obrigatórios.");
        }
        if (vidaMaxima < 1 || ataque < 0 || defesa < 0 || manaMaxima < 0 || alcance < 1 || movimentoMaximo < 0) {
            throw new IllegalArgumentException("Os atributos iniciais do personagem são inválidos.");
        }

        this.tipo = tipo;
        this.nome = nome.trim();
        this.vidaMaxima = vidaMaxima;
        this.vidaAtual = vidaMaxima;
        this.ataque = ataque;
        this.defesa = defesa;
        this.manaMaxima = manaMaxima;
        this.manaAtual = manaMaxima;
        this.alcance = alcance;
        this.movimentoMaximo = movimentoMaximo;
        this.habilidadeEspecial = habilidadeEspecial;
    }

    public TipoPersonagem getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public int getVidaAtual() {
        return vidaAtual;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefesa() {
        return defesa;
    }

    public int getManaMaxima() {
        return manaMaxima;
    }

    public int getManaAtual() {
        return manaAtual;
    }

    public int getAlcance() {
        return alcance;
    }

    public int getMovimentoMaximo() {
        return movimentoMaximo;
    }

    public Optional<Habilidade> getHabilidadeEspecial() {
        return Optional.ofNullable(habilidadeEspecial);
    }

    public boolean estaVivo() {
        return vidaAtual > 0;
    }

    /** Aplica dano bruto; defesa permanece informativa até a regra ser definida na integração. */
    public int receberDano(int dano) {
        validarQuantidade(dano, "dano");
        if (!estaVivo()) {
            return 0;
        }

        int vidaAnterior = vidaAtual;
        vidaAtual = Math.max(0, vidaAtual - dano);
        return vidaAnterior - vidaAtual;
    }

    public int recuperarVida(int quantidade) {
        validarQuantidade(quantidade, "recuperação de vida");
        int vidaAnterior = vidaAtual;
        vidaAtual = (int) Math.min(vidaMaxima, (long) vidaAtual + quantidade);
        return vidaAtual - vidaAnterior;
    }

    public boolean consumirMana(int quantidade) {
        validarQuantidade(quantidade, "consumo de mana");
        if (manaAtual < quantidade) {
            return false;
        }

        manaAtual -= quantidade;
        return true;
    }

    public int recuperarMana(int quantidade) {
        validarQuantidade(quantidade, "recuperação de mana");
        int manaAnterior = manaAtual;
        manaAtual = (int) Math.min(manaMaxima, (long) manaAtual + quantidade);
        return manaAtual - manaAnterior;
    }

    /** A distância informada deve ser calculada pela regra Manhattan do tabuleiro. */
    public ResultadoAcao realizarAtaqueBasico(Personagem alvo, int distanciaManhattan) {
        if (!estaVivo()) {
            return ResultadoAcao.falha(nome + " não pode atacar porque está derrotado.");
        }
        if (alvo == null || alvo == this || !alvo.estaVivo()) {
            return ResultadoAcao.falha("O alvo não pode receber o ataque.");
        }
        if (distanciaManhattan < 0 || distanciaManhattan > alcance) {
            return ResultadoAcao.falha("O alvo está fora do alcance do ataque básico.");
        }

        int dano = alvo.receberDano(ataque);
        return ResultadoAcao.sucesso(dano, nome + " causou " + dano + " de dano com um ataque básico.");
    }

    public abstract ResultadoAcao executarHabilidadeEspecial(Personagem alvo, int distanciaManhattan);

    protected ResultadoAcao aplicarHabilidadeEspecial(Personagem alvo, int distanciaManhattan) {
        if (habilidadeEspecial == null) {
            return ResultadoAcao.falha(nome + " não possui habilidade especial.");
        }
        if (!estaVivo()) {
            return ResultadoAcao.falha(nome + " não pode usar habilidade porque está derrotado.");
        }
        if (alvo == null || alvo == this || !alvo.estaVivo()) {
            return ResultadoAcao.falha("O alvo não pode receber a habilidade.");
        }
        if (distanciaManhattan < 0 || distanciaManhattan > habilidadeEspecial.getAlcance()) {
            return ResultadoAcao.falha("O alvo está fora do alcance de " + habilidadeEspecial.getNome() + ".");
        }
        if (!consumirMana(habilidadeEspecial.getCustoMana())) {
            return ResultadoAcao.falha("Mana insuficiente para " + habilidadeEspecial.getNome() + ".");
        }

        int dano = alvo.receberDano(habilidadeEspecial.getDano());
        return ResultadoAcao.sucesso(
                dano,
                nome + " usou " + habilidadeEspecial.getNome() + " e causou " + dano + " de dano.");
    }

    private static void validarQuantidade(int quantidade, String campo) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade de " + campo + " não pode ser negativa.");
        }
    }
}
