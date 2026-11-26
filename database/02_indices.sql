-- Indice dedicado ao historico de um jogador, ordenado pelas partidas mais recentes.
-- PKs e constraints UNIQUE ja criam seus indices automaticamente; este indice atende
-- a consulta de historico e exclui as partidas ainda sem data de finalizacao.

CREATE INDEX IF NOT EXISTS idx_partidas_historico_jogador
    ON arena.partidas (jogador_id, finalizada_em DESC)
    WHERE finalizada_em IS NOT NULL;
