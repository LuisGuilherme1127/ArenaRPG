CREATE OR REPLACE VIEW arena.vw_ranking_jogadores
WITH (security_barrier = true)
AS
SELECT
    j.id AS jogador_id,
    j.apelido,
    COALESCE(e.partidas_finalizadas, 0) AS partidas_finalizadas,
    COALESCE(e.vitorias, 0) AS vitorias,
    COALESCE(e.derrotas, 0) AS derrotas,
    COALESCE(
        ROUND(100::numeric * e.vitorias / NULLIF(e.partidas_finalizadas, 0), 2),
        0.00::numeric
    ) AS taxa_vitorias_percentual
FROM arena.jogadores AS j
LEFT JOIN arena.estatisticas_jogadores AS e
       ON e.jogador_id = j.id;

COMMENT ON VIEW arena.vw_ranking_jogadores IS
    'Ranking publico sem email, senha_hash ou outros dados de autenticacao.';

-- Consulta de exemplo para o DBeaver:
-- SELECT * FROM arena.vw_ranking_jogadores
-- ORDER BY vitorias DESC, taxa_vitorias_percentual DESC, apelido ASC;
