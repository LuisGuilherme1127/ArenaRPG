package com.arenarpg.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.exception.PersonagemNaoEncontradoException;

class PersonagemServiceTest {
    private final PersonagemService service = new PersonagemService();

    @Test
    void listaQuatroPersonagensEmOrdemConhecida() {
        var personagens = service.listarPersonagens();

        assertEquals(4, personagens.size());
        assertEquals("guerreiro", personagens.get(0).tipo());
        assertEquals("mago", personagens.get(1).tipo());
        assertEquals("arqueiro", personagens.get(2).tipo());
        assertEquals("goblin", personagens.get(3).tipo());
    }

    @Test
    void consultaPersonagemEConverteTipoSemDiferenciarMaiusculas() {
        PersonagemDTO mago = service.buscarPorTipo("  MAGO ");

        assertEquals("mago", mago.tipo());
        assertEquals(80, mago.vidaMaxima());
        assertEquals("Bola de Fogo", mago.habilidades().getFirst().nome());
    }

    @Test
    void tipoInexistenteGeraExcecaoDeDominio() {
        assertThrows(PersonagemNaoEncontradoException.class, () -> service.buscarPorTipo("dragao"));
    }
}
