package com.arenarpg.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PartidaControllerTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void cicloDePartidaRespondeComSnapshotsJson() throws Exception {
        String resposta = mockMvc.perform(post("/api/partidas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoPersonagem\":\"mago\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("em_andamento"))
                .andExpect(jsonPath("$.jogador.manaAtual").value(100))
                .andReturn().getResponse().getContentAsString();
        String id = resposta.replaceFirst("(?s).*?\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");

        mockMvc.perform(get("/api/partidas/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tabuleiro.linhas").value(8));
        mockMvc.perform(post("/api/partidas/{id}/mover", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":2,\"y\":6}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.movimentoRealizado").value(true));
        mockMvc.perform(post("/api/partidas/{id}/habilidade", id))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/partidas/{id}/finalizar-turno", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rodada").value(2));
    }

    @Test
    void errosDeEntradaEstadoEIdentificadorUsamStatusApropriado() throws Exception {
        mockMvc.perform(post("/api/partidas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoPersonagem\":\"dragao\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/partidas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoPersonagem\":\"mago\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/partidas/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/partidas/00000000-0000-0000-0000-000000000000/mover")
                .contentType(MediaType.APPLICATION_JSON).content("{\"x\":9,\"y\":0}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/partidas/not-a-uuid/atacar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endpointsDeAtaqueBasicoEHabilidadeAplicamEfeitoQuandoEstaoAoAlcance() throws Exception {
        String warrior = iniciar("guerreiro");
        mover(warrior, 4, 6);
        finalizar(warrior);
        mover(warrior, 4, 4);
        finalizar(warrior);
        mockMvc.perform(post("/api/partidas/{id}/atacar", warrior))
                .andExpect(status().isOk()).andExpect(jsonPath("$.inimigo.vidaAtual").value(80));

        String mage = iniciar("mago");
        mover(mage, 4, 6);
        finalizar(mage);
        mover(mage, 4, 4);
        finalizar(mage);
        mockMvc.perform(post("/api/partidas/{id}/habilidade", mage))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inimigo.vidaAtual").value(70))
                .andExpect(jsonPath("$.jogador.manaAtual").value(80));
    }

    private String iniciar(String tipo) throws Exception {
        String json = mockMvc.perform(post("/api/partidas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoPersonagem\":\"" + tipo + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.replaceFirst("(?s).*?\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private void mover(String id, int x, int y) throws Exception {
        mockMvc.perform(post("/api/partidas/{id}/mover", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":" + x + ",\"y\":" + y + "}"))
                .andExpect(status().isOk());
    }

    private void finalizar(String id) throws Exception {
        mockMvc.perform(post("/api/partidas/{id}/finalizar-turno", id)).andExpect(status().isOk());
    }
}
