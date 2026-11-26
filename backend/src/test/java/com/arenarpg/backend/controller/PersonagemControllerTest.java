package com.arenarpg.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PersonagemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaPersonagensComoJson() throws Exception {
        mockMvc.perform(get("/api/personagens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(4)))
                .andExpect(jsonPath("$[0].tipo").value("guerreiro"))
                .andExpect(jsonPath("$[0].habilidades[0].nome").value("Golpe Poderoso"));
    }

    @Test
    void consultaPersonagemPorTipo() throws Exception {
        mockMvc.perform(get("/api/personagens/arqueiro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vidaMaxima").value(90))
                .andExpect(jsonPath("$.habilidades[0].nome").value("Flecha Precisa"))
                .andExpect(jsonPath("$.habilidades[0].alcance").value(4));
    }

    @Test
    void tipoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/personagens/dragao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Personagem não encontrado"));
    }

    @Test
    void endpointDeStatusExistenteContinuaRespondendo() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string("Arena RPG - Backend Java funcionando!"));
    }
}
