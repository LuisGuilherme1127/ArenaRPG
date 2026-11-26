package com.arenarpg.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.arenarpg.backend.exception.ConflitoPartidaException;
import com.arenarpg.backend.model.partida.Posicao;

class PartidaServiceTest {
    private final PartidaService service = new PartidaService(new PersonagemService(), new ComportamentoGoblin());

    @Test
    void novasPartidasTemEntidadesIndependentes() {
        var primeira = service.iniciarPartida("guerreiro");
        var segunda = service.iniciarPartida("guerreiro");
        assertNotEquals(primeira.id(), segunda.id());
        service.mover(primeira.id(), new Posicao(2, 6));
        assertEquals(new Posicao(2, 6), new Posicao(
                service.buscarPorId(primeira.id()).tabuleiro().posicaoJogador().x(),
                service.buscarPorId(primeira.id()).tabuleiro().posicaoJogador().y()));
        assertEquals(new Posicao(1, 6), new Posicao(
                service.buscarPorId(segunda.id()).tabuleiro().posicaoJogador().x(),
                service.buscarPorId(segunda.id()).tabuleiro().posicaoJogador().y()));
        assertEquals(100, service.buscarPorId(primeira.id()).inimigo().vidaAtual());
        assertEquals(100, service.buscarPorId(segunda.id()).inimigo().vidaAtual());
        assertThrows(RuntimeException.class, () -> service.iniciarPartida("goblin"));
    }

    @Test
    void movimentosConcorrentesNaMesmaPartidaSaoSerializados() throws Exception {
        var partida = service.iniciarPartida("guerreiro");
        CountDownLatch iniciar = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger conflitos = new AtomicInteger();
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<?>> futures = new ArrayList<>();
            for (Posicao destino : List.of(new Posicao(2, 6), new Posicao(1, 5))) {
                futures.add(executor.submit(() -> {
                    iniciar.await();
                    try {
                        service.mover(partida.id(), destino);
                        sucessos.incrementAndGet();
                    } catch (ConflitoPartidaException exception) {
                        conflitos.incrementAndGet();
                    }
                    return null;
                }));
            }
            iniciar.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        }
        assertEquals(1, sucessos.get());
        assertEquals(1, conflitos.get());
        assertTrue(service.buscarPorId(partida.id()).movimentoRealizado());
    }
}
