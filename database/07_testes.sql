-- Executar somente depois dos scripts 01 a 06, conectado como administrador.
-- Todos os dados de teste ficam em uma transacao revertida ao final.
BEGIN;

DO $testes$
DECLARE
    v_jogador bigint;
    v_jogador_sem_partida bigint;
    v_guerreiro bigint;
    v_mago bigint;
    v_arqueiro bigint;
    v_goblin bigint;
    v_partida_vitoria bigint;
    v_partida_derrota bigint;
    v_partida_aberta bigint;
    v_rejeitada boolean;
    v_total bigint;
    v_vitorias bigint;
    v_derrotas bigint;
    v_tag text;
    v_mensagem text;
    v_estado text;
BEGIN
    SELECT id INTO STRICT v_guerreiro FROM arena.personagens WHERE tipo = 'guerreiro' AND vida_maxima = 120 AND ataque = 20 AND defesa = 0 AND mana_maxima = 30 AND alcance = 1 AND movimento_maximo = 3 AND jogavel;
    SELECT id INTO STRICT v_mago FROM arena.personagens WHERE tipo = 'mago' AND vida_maxima = 80 AND ataque = 15 AND defesa = 0 AND mana_maxima = 100 AND alcance = 3 AND movimento_maximo = 3 AND jogavel;
    SELECT id INTO STRICT v_arqueiro FROM arena.personagens WHERE tipo = 'arqueiro' AND vida_maxima = 90 AND ataque = 18 AND defesa = 0 AND mana_maxima = 50 AND alcance = 4 AND movimento_maximo = 3 AND jogavel;
    SELECT id INTO STRICT v_goblin FROM arena.personagens WHERE tipo = 'goblin' AND vida_maxima = 100 AND ataque = 12 AND defesa = 0 AND mana_maxima = 0 AND alcance = 1 AND movimento_maximo = 2 AND NOT jogavel;

    IF NOT EXISTS (SELECT 1 FROM arena.habilidades h JOIN arena.personagens p ON p.id = h.personagem_id WHERE p.tipo = 'guerreiro' AND h.nome = 'Golpe Poderoso' AND h.dano = 35 AND h.custo_mana = 10 AND h.alcance = 1)
       OR NOT EXISTS (SELECT 1 FROM arena.habilidades h JOIN arena.personagens p ON p.id = h.personagem_id WHERE p.tipo = 'mago' AND h.nome = 'Bola de Fogo' AND h.dano = 30 AND h.custo_mana = 20 AND h.alcance = 3)
       OR NOT EXISTS (SELECT 1 FROM arena.habilidades h JOIN arena.personagens p ON p.id = h.personagem_id WHERE p.tipo = 'arqueiro' AND h.nome = 'Flecha Precisa' AND h.dano = 28 AND h.custo_mana = 15 AND h.alcance = 4) THEN
        RAISE EXCEPTION 'Catalogo inicial de habilidades nao corresponde ao Java.';
    END IF;

    v_tag := substr(md5(clock_timestamp()::text || pg_backend_pid()::text), 1, 10);
    INSERT INTO arena.jogadores (apelido, email, senha_hash)
    VALUES ('teste_etapa15_' || v_tag, 'teste.etapa15.' || v_tag || '@example.invalid', 'hash_de_teste_nao_autenticavel')
    RETURNING id INTO v_jogador;
    INSERT INTO arena.jogadores (apelido, email, senha_hash)
    VALUES ('sem_partida_' || v_tag, 'sem.partida.' || v_tag || '@example.invalid', 'hash_de_teste_nao_autenticavel')
    RETURNING id INTO v_jogador_sem_partida;

    -- Integridade de entidade: apelido duplicado deve falhar.
    v_rejeitada := false;
    BEGIN
        INSERT INTO arena.jogadores (apelido, email, senha_hash)
        VALUES ('teste_etapa15_' || v_tag, 'duplicado.' || v_tag || '@example.invalid', 'hash');
    EXCEPTION WHEN unique_violation THEN
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'UNIQUE de jogadores nao rejeitou duplicidade.'; END IF;

    -- Integridade de dominio: apelido com espacos nas bordas deve falhar.
    v_rejeitada := false;
    BEGIN
        INSERT INTO arena.jogadores (apelido, email, senha_hash)
        VALUES (' apelido_invalido ', 'espacos@example.invalid', 'hash');
    EXCEPTION WHEN check_violation THEN
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'CHECK de apelido nao rejeitou valor invalido.'; END IF;

    -- Integridade referencial: jogador inexistente deve falhar.
    v_rejeitada := false;
    BEGIN
        INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
        VALUES (-999999, v_guerreiro, v_goblin);
    EXCEPTION WHEN foreign_key_violation THEN
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'FK de partidas nao rejeitou jogador inexistente.'; END IF;

    -- Regra definida pelo usuario/trigger: nao se pode iniciar com Goblin como classe jogavel.
    v_rejeitada := false;
    BEGIN
        INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
        VALUES (v_jogador, v_goblin, v_guerreiro);
    EXCEPTION WHEN check_violation THEN
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'Validacao de personagem jogavel nao foi aplicada.'; END IF;

    v_rejeitada := false;
    BEGIN
        INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
        VALUES (v_jogador, v_guerreiro, v_mago);
    EXCEPTION WHEN check_violation THEN
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'Validacao de Goblin como inimigo nao foi aplicada.'; END IF;

    INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
    VALUES (v_jogador, v_guerreiro, v_goblin)
    RETURNING id INTO v_partida_vitoria;
    SELECT partidas_finalizadas, vitorias, derrotas INTO v_total, v_vitorias, v_derrotas
      FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF FOUND THEN RAISE EXCEPTION 'Partida em andamento foi contabilizada.'; END IF;

    UPDATE arena.partidas SET estado = 'vitoria', rodadas = 2 WHERE id = v_partida_vitoria;
    SELECT partidas_finalizadas, vitorias, derrotas INTO STRICT v_total, v_vitorias, v_derrotas
      FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF (v_total, v_vitorias, v_derrotas) IS DISTINCT FROM (1, 1, 0) THEN
        RAISE EXCEPTION 'Contabilizacao de vitoria incorreta: %, %, %.', v_total, v_vitorias, v_derrotas;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM arena.partidas WHERE id = v_partida_vitoria AND finalizada_em IS NOT NULL) THEN
        RAISE EXCEPTION 'Trigger nao preencheu finalizada_em.';
    END IF;

    -- Uma segunda partida verifica o caminho de derrota e a soma agregada.
    INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
    VALUES (v_jogador, v_guerreiro, v_goblin)
    RETURNING id INTO v_partida_derrota;
    UPDATE arena.partidas SET estado = 'derrota' WHERE id = v_partida_derrota;
    SELECT partidas_finalizadas, vitorias, derrotas INTO STRICT v_total, v_vitorias, v_derrotas
      FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF (v_total, v_vitorias, v_derrotas) IS DISTINCT FROM (2, 1, 1) THEN
        RAISE EXCEPTION 'Contabilizacao de derrota incorreta: %, %, %.', v_total, v_vitorias, v_derrotas;
    END IF;

    -- Partida finalizada nao pode ser recontada, alterada ou apagada.
    v_rejeitada := false;
    BEGIN
        UPDATE arena.partidas SET estado = 'vitoria' WHERE id = v_partida_vitoria;
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS v_mensagem = MESSAGE_TEXT;
        IF v_mensagem NOT LIKE 'Partidas finalizadas sao imutaveis%' THEN
            RAISE;
        END IF;
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'Atualizacao de partida finalizada foi permitida.'; END IF;

    v_rejeitada := false;
    BEGIN
        DELETE FROM arena.partidas WHERE id = v_partida_vitoria;
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS v_mensagem = MESSAGE_TEXT;
        IF v_mensagem NOT LIKE 'Partidas finalizadas nao podem ser excluidas%' THEN
            RAISE;
        END IF;
        v_rejeitada := true;
    END;
    IF NOT v_rejeitada THEN RAISE EXCEPTION 'Exclusao de partida finalizada foi permitida.'; END IF;

    SELECT estado INTO STRICT v_estado FROM arena.partidas WHERE id = v_partida_vitoria;
    SELECT partidas_finalizadas, vitorias, derrotas INTO STRICT v_total, v_vitorias, v_derrotas
      FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF v_estado <> 'vitoria' OR (v_total, v_vitorias, v_derrotas) IS DISTINCT FROM (2, 1, 1) THEN
        RAISE EXCEPTION 'Ação recusada alterou uma partida finalizada ou duplicou estatísticas.';
    END IF;

    INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
    VALUES (v_jogador, v_guerreiro, v_goblin)
    RETURNING id INTO v_partida_aberta;
    DELETE FROM arena.partidas WHERE id = v_partida_aberta;
    SELECT partidas_finalizadas, vitorias, derrotas INTO STRICT v_total, v_vitorias, v_derrotas
      FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF (v_total, v_vitorias, v_derrotas) IS DISTINCT FROM (2, 1, 1) THEN
        RAISE EXCEPTION 'Partida removida ainda em andamento alterou estatisticas.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM arena.vw_ranking_jogadores WHERE jogador_id = v_jogador AND partidas_finalizadas = 2 AND vitorias = 1 AND derrotas = 1 AND taxa_vitorias_percentual = 50.00) THEN
        RAISE EXCEPTION 'View de ranking nao retornou as estatisticas esperadas.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM arena.vw_ranking_jogadores WHERE jogador_id = v_jogador_sem_partida AND partidas_finalizadas = 0 AND taxa_vitorias_percentual = 0.00) THEN
        RAISE EXCEPTION 'View nao tratou jogador sem partidas/divisao por zero.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_indexes WHERE schemaname = 'arena' AND indexname = 'idx_partidas_historico_jogador') THEN
        RAISE EXCEPTION 'Indice de historico nao foi criado.';
    END IF;
    IF NOT pg_catalog.has_column_privilege('arena_app', 'arena.partidas', 'jogador_id', 'INSERT')
       OR pg_catalog.has_table_privilege('arena_consulta', 'arena.jogadores', 'SELECT')
       OR NOT pg_catalog.has_table_privilege('arena_consulta', 'arena.vw_ranking_jogadores', 'SELECT') THEN
        RAISE EXCEPTION 'Permissoes das roles nao correspondem ao privilegio minimo esperado.';
    END IF;

    RAISE NOTICE 'Testes SQL da Etapa 15 passaram nesta transacao; os dados serao revertidos.';
END;
$testes$;

ROLLBACK;

-- Teste de concorrencia requer duas sessoes DBeaver e banco descartavel.
-- O procedimento manual esta documentado em database/README.md; nao faz parte desta transacao.
