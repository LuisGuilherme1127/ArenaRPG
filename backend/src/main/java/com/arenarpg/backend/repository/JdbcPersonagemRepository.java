package com.arenarpg.backend.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.arenarpg.backend.dto.HabilidadeDTO;
import com.arenarpg.backend.dto.PersonagemDTO;
import com.arenarpg.backend.exception.CatalogoInconsistenteException;
import com.arenarpg.backend.service.PersonagemService;

@Repository
@Profile("postgres")
public class JdbcPersonagemRepository implements PersonagemCatalogo {
    private static final String SELECT_CATALOGO = """
            SELECT p.tipo,
                   p.nome,
                   p.vida_maxima,
                   p.ataque,
                   p.defesa,
                   p.mana_maxima,
                   p.alcance,
                   p.movimento_maximo,
                   h.nome AS habilidade_nome,
                   h.descricao AS habilidade_descricao,
                   h.dano AS habilidade_dano,
                   h.custo_mana AS habilidade_custo_mana,
                   h.alcance AS habilidade_alcance,
                   h.tipo AS habilidade_tipo
              FROM arena.personagens p
              LEFT JOIN arena.habilidades h ON h.personagem_id = p.id
            """;

    private static final String ORDER_CATALOGO = """
             ORDER BY CASE p.tipo
                          WHEN 'guerreiro' THEN 1
                          WHEN 'mago' THEN 2
                          WHEN 'arqueiro' THEN 3
                          WHEN 'goblin' THEN 4
                          ELSE 5
                      END,
                      h.id
            """;

    private final JdbcTemplate jdbcTemplate;
    private final PersonagemService personagemService;

    public JdbcPersonagemRepository(JdbcTemplate jdbcTemplate, PersonagemService personagemService) {
        this.jdbcTemplate = jdbcTemplate;
        this.personagemService = personagemService;
    }

    @Override
    public List<PersonagemDTO> listar() {
        return validarConsistencia(jdbcTemplate.query(SELECT_CATALOGO + ORDER_CATALOGO, agruparPersonagens()));
    }

    @Override
    public PersonagemDTO buscarPorTipo(String tipo) {
        PersonagemDTO personagemJava = personagemService.buscarPorTipo(tipo);
        List<PersonagemDTO> encontrados = jdbcTemplate.query(
                SELECT_CATALOGO + " WHERE p.tipo = ? " + ORDER_CATALOGO,
                agruparPersonagens(),
                tipo == null ? "" : tipo.trim().toLowerCase(Locale.ROOT));

        if (encontrados.isEmpty()) {
            throw new CatalogoInconsistenteException();
        }
        PersonagemDTO encontrado = encontrados.getFirst();
        if (!encontrado.equals(personagemJava)) {
            throw new CatalogoInconsistenteException();
        }
        return encontrado;
    }

    private List<PersonagemDTO> validarConsistencia(List<PersonagemDTO> personagensBanco) {
        Map<String, PersonagemDTO> personagensJava = new HashMap<>();
        personagemService.listarPersonagens().forEach(personagem -> personagensJava.put(personagem.tipo(), personagem));
        boolean consistente = personagensBanco.size() == personagensJava.size()
                && personagensBanco.stream().allMatch(personagem -> personagem.equals(personagensJava.get(personagem.tipo())));
        if (!consistente) {
            throw new CatalogoInconsistenteException();
        }
        return personagensBanco;
    }

    private ResultSetExtractor<List<PersonagemDTO>> agruparPersonagens() {
        return resultSet -> {
            Map<String, PersonagemLinha> personagens = new LinkedHashMap<>();
            while (resultSet.next()) {
                PersonagemLinha personagem = personagens.computeIfAbsent(
                        lerTipo(resultSet), ignored -> criarPersonagem(resultSet));
                String nomeHabilidade = resultSet.getString("habilidade_nome");
                if (nomeHabilidade != null) {
                    personagem.habilidades().add(new HabilidadeDTO(
                            nomeHabilidade,
                            resultSet.getString("habilidade_descricao"),
                            resultSet.getInt("habilidade_dano"),
                            resultSet.getInt("habilidade_custo_mana"),
                            resultSet.getInt("habilidade_alcance"),
                            resultSet.getString("habilidade_tipo").toLowerCase(Locale.ROOT)));
                }
            }
            return personagens.values().stream().map(PersonagemLinha::paraDto).toList();
        };
    }

    private static String lerTipo(ResultSet resultSet) {
        try {
            return resultSet.getString("tipo");
        } catch (SQLException exception) {
            throw new org.springframework.jdbc.UncategorizedSQLException("Ler tipo de personagem", SELECT_CATALOGO,
                    exception);
        }
    }

    private static PersonagemLinha criarPersonagem(ResultSet resultSet) {
        try {
            int vidaMaxima = resultSet.getInt("vida_maxima");
            int manaMaxima = resultSet.getInt("mana_maxima");
            return new PersonagemLinha(
                    resultSet.getString("tipo"),
                    resultSet.getString("nome"),
                    vidaMaxima,
                    resultSet.getInt("ataque"),
                    resultSet.getInt("defesa"),
                    manaMaxima,
                    resultSet.getInt("alcance"),
                    resultSet.getInt("movimento_maximo"),
                    new ArrayList<>());
        } catch (SQLException exception) {
            throw new org.springframework.jdbc.UncategorizedSQLException("Ler personagem", SELECT_CATALOGO,
                    exception);
        }
    }

    private record PersonagemLinha(
            String tipo,
            String nome,
            int vidaMaxima,
            int ataque,
            int defesa,
            int manaMaxima,
            int alcance,
            int movimentoMaximo,
            List<HabilidadeDTO> habilidades) {
        private PersonagemDTO paraDto() {
            // As tabelas armazenam máximos, e novos personagens iniciam com vida/mana cheias.
            return new PersonagemDTO(tipo, nome, vidaMaxima, vidaMaxima, ataque, defesa, manaMaxima, manaMaxima,
                    alcance, movimentoMaximo, habilidades);
        }
    }
}

