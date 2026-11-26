package com.arenarpg.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.arenarpg.backend.model.characters.Arqueiro;
import com.arenarpg.backend.model.characters.Goblin;
import com.arenarpg.backend.model.characters.Guerreiro;
import com.arenarpg.backend.model.characters.Mago;

class PersonagemTest {
    @Test
    void classesJogaveisIniciamComAtributosDoFrontend() {
        Personagem guerreiro = new Guerreiro();
        Personagem mago = new Mago();
        Personagem arqueiro = new Arqueiro();

        assertAtributos(guerreiro, 120, 30, 20, 1, 3);
        assertAtributos(mago, 80, 100, 15, 3, 3);
        assertAtributos(arqueiro, 90, 50, 18, 4, 3);
        assertEquals(0, guerreiro.getDefesa());
        assertEquals(0, mago.getDefesa());
        assertEquals(0, arqueiro.getDefesa());
    }

    @Test
    void goblinIniciaComAtributosEAlcanceDeMovimentoDoFrontend() {
        Personagem goblin = new Goblin();

        assertAtributos(goblin, 100, 0, 12, 1, 2);
        assertTrue(goblin.getHabilidadeEspecial().isEmpty());
    }

    @Test
    void vidaEManaRespeitamLimitesAoReceberDanoERecuperacao() {
        Personagem personagem = new Guerreiro();

        assertEquals(120, personagem.getVidaMaxima());
        assertEquals(120, personagem.receberDano(500));
        assertEquals(0, personagem.getVidaAtual());
        assertEquals(120, personagem.recuperarVida(500));
        assertEquals(120, personagem.getVidaAtual());
        assertEquals(0, personagem.recuperarVida(Integer.MAX_VALUE));
        assertEquals(100, personagem.receberDano(100));
        assertEquals(100, personagem.recuperarVida(Integer.MAX_VALUE));
        assertEquals(120, personagem.receberDano(500));
        assertEquals(0, personagem.getVidaAtual());

        assertTrue(personagem.consumirMana(10));
        assertEquals(10, personagem.recuperarMana(10));
        assertEquals(30, personagem.getManaAtual());
        assertEquals(0, personagem.recuperarMana(10));
        assertEquals(0, personagem.recuperarMana(Integer.MAX_VALUE));
        assertTrue(personagem.consumirMana(20));
        assertEquals(20, personagem.recuperarMana(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> personagem.receberDano(-1));
        assertThrows(IllegalArgumentException.class, () -> personagem.recuperarVida(-1));
        assertThrows(IllegalArgumentException.class, () -> personagem.consumirMana(-1));
    }

    @Test
    void receberDanoReduzVidaEPersonagemDerrotadoNaoAge() {
        Personagem guerreiro = new Guerreiro();
        Personagem goblin = new Goblin();

        assertEquals(20, goblin.receberDano(20));
        assertEquals(80, goblin.getVidaAtual());
        assertEquals(80, goblin.receberDano(120));
        assertFalse(goblin.estaVivo());

        ResultadoAcao ataque = goblin.realizarAtaqueBasico(guerreiro, 1);
        ResultadoAcao habilidade = goblin.executarHabilidadeEspecial(guerreiro, 1);
        assertFalse(ataque.sucesso());
        assertFalse(habilidade.sucesso());
        assertEquals(120, guerreiro.getVidaAtual());
    }

    @Test
    void ataqueBasicoCausaDanoIgualAoAtributoAtaque() {
        Personagem guerreiro = new Guerreiro();
        Personagem goblin = new Goblin();

        ResultadoAcao foraDoAlcance = guerreiro.realizarAtaqueBasico(goblin, 2);
        assertFalse(foraDoAlcance.sucesso());
        assertEquals(100, goblin.getVidaAtual());

        ResultadoAcao resultado = guerreiro.realizarAtaqueBasico(goblin, 1);

        assertTrue(resultado.sucesso());
        assertEquals(20, resultado.danoCausado());
        assertEquals(80, goblin.getVidaAtual());
    }

    @Test
    void habilidadesEspeciaisSaoPolimorficasEDiferemEmDanoCustoEAlcance() {
        Personagem guerreiro = new Guerreiro();
        Personagem mago = new Mago();
        Personagem arqueiro = new Arqueiro();

        assertHabilidade(guerreiro, new Goblin(), "Golpe Poderoso", 35, 10, 1);
        assertHabilidade(mago, new Goblin(), "Bola de Fogo", 30, 20, 3);
        assertHabilidade(arqueiro, new Goblin(), "Flecha Precisa", 28, 15, 4);
        assertEquals(20, guerreiro.getManaAtual());
        assertEquals(80, mago.getManaAtual());
        assertEquals(35, arqueiro.getManaAtual());
    }

    @Test
    void habilidadeNaoConsomeManaNemCausaDanoQuandoManaEInsuficiente() {
        Personagem mago = new Mago();
        Personagem goblin = new Goblin();
        assertTrue(mago.consumirMana(81));

        ResultadoAcao resultado = mago.executarHabilidadeEspecial(goblin, 3);

        assertFalse(resultado.sucesso());
        assertEquals(0, resultado.danoCausado());
        assertEquals(19, mago.getManaAtual());
        assertEquals(100, goblin.getVidaAtual());
    }

    @Test
    void habilidadeValidaCamposEAlcance() {
        assertThrows(IllegalArgumentException.class,
                () -> new Habilidade(" ", "Descrição", 1, 0, 1, TipoHabilidade.FISICA));
        assertThrows(IllegalArgumentException.class,
                () -> new Habilidade("Golpe", "Descrição", 1, 0, 0, TipoHabilidade.FISICA));
        assertThrows(IllegalArgumentException.class,
                () -> new Habilidade("Golpe", "Descrição", -1, 0, 1, TipoHabilidade.FISICA));
    }

    private static void assertAtributos(
            Personagem personagem,
            int vida,
            int mana,
            int ataque,
            int alcance,
            int movimento) {
        assertEquals(vida, personagem.getVidaMaxima());
        assertEquals(vida, personagem.getVidaAtual());
        assertEquals(mana, personagem.getManaMaxima());
        assertEquals(mana, personagem.getManaAtual());
        assertEquals(ataque, personagem.getAtaque());
        assertEquals(alcance, personagem.getAlcance());
        assertEquals(movimento, personagem.getMovimentoMaximo());
        assertTrue(personagem.estaVivo());
    }

    private static void assertHabilidade(
            Personagem personagem,
            Personagem alvo,
            String nome,
            int dano,
            int custoMana,
            int alcance) {
        Habilidade habilidade = personagem.getHabilidadeEspecial().orElseThrow();
        assertEquals(nome, habilidade.getNome());
        assertEquals(dano, habilidade.getDano());
        assertEquals(custoMana, habilidade.getCustoMana());
        assertEquals(alcance, habilidade.getAlcance());

        ResultadoAcao foraDoAlcance = personagem.executarHabilidadeEspecial(alvo, alcance + 1);
        assertFalse(foraDoAlcance.sucesso());
        assertEquals(100, alvo.getVidaAtual());
        assertEquals(personagem.getManaMaxima(), personagem.getManaAtual());

        ResultadoAcao resultado = personagem.executarHabilidadeEspecial(alvo, alcance);
        assertTrue(resultado.sucesso());
        assertEquals(dano, resultado.danoCausado());
        assertEquals(100 - dano, alvo.getVidaAtual());
    }
}
