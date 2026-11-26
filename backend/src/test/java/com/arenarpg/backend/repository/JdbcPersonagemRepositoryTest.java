package com.arenarpg.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.exception.CatalogoInconsistenteException;
import com.arenarpg.backend.model.characters.Mago;
import com.arenarpg.backend.service.PersonagemService;

class JdbcPersonagemRepositoryTest {
    private JdbcTemplate jdbcTemplate;
    private PersonagemService personagemService;
    private JdbcPersonagemRepository repository;

    @BeforeEach
    void configurar() {
        jdbcTemplate = mock(JdbcTemplate.class);
        personagemService = new PersonagemService();
        repository = new JdbcPersonagemRepository(jdbcTemplate, personagemService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void usaJoinParametroEMapeiaARespostaParaOContratoDoFrontend() throws Exception {
        ResultSet resultSet = mockMagoRow(80);

        when(jdbcTemplate.query(anyString(), anyExtractor(), eq("mago"))).thenAnswer(invocation -> {
            ResultSetExtractor<List<PersonagemDTO>> extractor = invocation.getArgument(1);
            return extractor.extractData(resultSet);
        });

        PersonagemDTO personagem = repository.buscarPorTipo("MAGO");

        assertEquals(PersonagemDTO.de(new Mago()), personagem);
        assertTrue(capturarSql().contains("LEFT JOIN arena.habilidades"));
        assertTrue(capturarSql().contains("WHERE p.tipo = ?"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejeitaDadosQueDivergemDosAtributosDoDominioJava() throws Exception {
        ResultSet resultSet = mockMagoRow(81);
        when(jdbcTemplate.query(anyString(), anyExtractor(), eq("mago"))).thenAnswer(invocation -> {
            ResultSetExtractor<List<PersonagemDTO>> extractor = invocation.getArgument(1);
            return extractor.extractData(resultSet);
        });

        assertThrows(CatalogoInconsistenteException.class, () -> repository.buscarPorTipo("mago"));
    }

    private ResultSet mockMagoRow(int vidaMaxima) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("tipo")).thenReturn("mago");
        when(resultSet.getString("nome")).thenReturn("Mago");
        when(resultSet.getInt("vida_maxima")).thenReturn(vidaMaxima);
        when(resultSet.getInt("ataque")).thenReturn(15);
        when(resultSet.getInt("defesa")).thenReturn(0);
        when(resultSet.getInt("mana_maxima")).thenReturn(100);
        when(resultSet.getInt("alcance")).thenReturn(3);
        when(resultSet.getInt("movimento_maximo")).thenReturn(3);
        when(resultSet.getString("habilidade_nome")).thenReturn("Bola de Fogo");
        when(resultSet.getString("habilidade_descricao"))
                .thenReturn("Uma esfera de fogo que atinge um alvo à distância.");
        when(resultSet.getInt("habilidade_dano")).thenReturn(30);
        when(resultSet.getInt("habilidade_custo_mana")).thenReturn(20);
        when(resultSet.getInt("habilidade_alcance")).thenReturn(3);
        when(resultSet.getString("habilidade_tipo")).thenReturn("MAGICA");
        return resultSet;
    }

    @SuppressWarnings("unchecked")
    private static ResultSetExtractor<List<PersonagemDTO>> anyExtractor() {
        return (ResultSetExtractor<List<PersonagemDTO>>) any(ResultSetExtractor.class);
    }

    private String capturarSql() {
        org.mockito.ArgumentCaptor<String> sql = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(jdbcTemplate).query(sql.capture(), anyExtractor(), eq("mago"));
        return sql.getValue();
    }
}

