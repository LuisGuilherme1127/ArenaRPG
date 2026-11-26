-- Consultas somente de leitura para apresentação no DBeaver.
-- A demonstração transacional ao final é opcional e termina com ROLLBACK.

-- 1. Tabelas do schema.
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'arena'
ORDER BY table_name;

-- 2. Chaves primárias, estrangeiras, UNIQUE e CHECK.
SELECT t.relname AS tabela, c.conname AS constraint_name,
       CASE c.contype
           WHEN 'p' THEN 'PRIMARY KEY'
           WHEN 'f' THEN 'FOREIGN KEY'
           WHEN 'u' THEN 'UNIQUE'
           WHEN 'c' THEN 'CHECK'
           ELSE c.contype::text
       END AS tipo,
       pg_catalog.pg_get_constraintdef(c.oid) AS definicao
FROM pg_catalog.pg_constraint c
JOIN pg_catalog.pg_class t ON t.oid = c.conrelid
JOIN pg_catalog.pg_namespace n ON n.oid = t.relnamespace
WHERE n.nspname = 'arena'
ORDER BY t.relname, c.conname;

-- 3. Índices explícitos e índices criados por PK/UNIQUE.
SELECT tablename, indexname, indexdef
FROM pg_catalog.pg_indexes
WHERE schemaname = 'arena'
ORDER BY tablename, indexname;

-- 4. Triggers habilitadas e funções associadas.
SELECT t.tgname AS trigger_name,
       c.relname AS tabela,
       p.proname AS funcao,
       pg_catalog.pg_get_triggerdef(t.oid) AS definicao
FROM pg_catalog.pg_trigger t
JOIN pg_catalog.pg_class c ON c.oid = t.tgrelid
JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
JOIN pg_catalog.pg_proc p ON p.oid = t.tgfoid
WHERE n.nspname = 'arena'
  AND NOT t.tgisinternal
ORDER BY c.relname, t.tgname;

-- 5. Roles do projeto e privilégios administrativos gerais (não mostra segredos).
SELECT rolname, rolcanlogin, rolsuper, rolcreatedb, rolcreaterole, rolinherit
FROM pg_catalog.pg_roles
WHERE rolname IN ('arena_app', 'arena_consulta')
ORDER BY rolname;

SELECT roles.role_name,
       objetos.objeto,
       pg_catalog.has_table_privilege(roles.role_name::name, objetos.objeto, 'SELECT') AS pode_ler,
       pg_catalog.has_table_privilege(roles.role_name::name, objetos.objeto, 'INSERT') AS pode_inserir_tabela,
       pg_catalog.has_table_privilege(roles.role_name::name, objetos.objeto, 'UPDATE') AS pode_atualizar_tabela,
       pg_catalog.has_table_privilege(roles.role_name::name, objetos.objeto, 'DELETE') AS pode_excluir
FROM (VALUES ('arena_app'), ('arena_consulta')) AS roles(role_name)
CROSS JOIN (VALUES
    ('arena.jogadores'),
    ('arena.personagens'),
    ('arena.habilidades'),
    ('arena.partidas'),
    ('arena.estatisticas_jogadores'),
    ('arena.vw_ranking_jogadores')
) AS objetos(objeto)
ORDER BY roles.role_name, objetos.objeto;

SELECT pg_catalog.has_column_privilege('arena_app', 'arena.jogadores', 'senha_hash', 'SELECT') AS app_pode_ler_hash,
       pg_catalog.has_column_privilege('arena_app', 'arena.jogadores', 'senha_hash', 'INSERT') AS app_pode_inserir_hash,
       pg_catalog.has_column_privilege('arena_consulta', 'arena.jogadores', 'senha_hash', 'SELECT') AS consulta_pode_ler_hash,
       pg_catalog.has_column_privilege('arena_app', 'arena.partidas', 'jogador_id', 'INSERT') AS app_pode_iniciar_partida;

SELECT role_name,
       pg_catalog.has_database_privilege(role_name::name, current_database(), 'CONNECT') AS pode_conectar,
       pg_catalog.has_schema_privilege(role_name::name, 'arena', 'USAGE') AS pode_usar_schema,
       pg_catalog.has_table_privilege(role_name::name, 'arena.vw_ranking_jogadores', 'SELECT') AS pode_ler_ranking
FROM (VALUES ('arena_app'), ('arena_consulta')) AS roles(role_name)
ORDER BY role_name;

SELECT sequencename,
       pg_catalog.has_sequence_privilege('arena_app', format('%I.%I', schemaname, sequencename), 'USAGE') AS arena_app_pode_usar
FROM pg_catalog.pg_sequences
WHERE schemaname = 'arena'
ORDER BY sequencename;

-- 6. View e exemplo de ranking público.
SELECT table_name, view_definition
FROM information_schema.views
WHERE table_schema = 'arena' AND table_name = 'vw_ranking_jogadores';

SELECT jogador_id, apelido, partidas_finalizadas, vitorias, derrotas, taxa_vitorias_percentual
FROM arena.vw_ranking_jogadores
ORDER BY vitorias DESC, taxa_vitorias_percentual DESC, apelido ASC;

-- 7. Catálogo de personagens e habilidades: JOIN usado pela consulta JDBC.
SELECT p.tipo, p.nome, p.jogavel, p.vida_maxima, p.ataque, p.defesa,
       p.mana_maxima, p.alcance, p.movimento_maximo,
       h.nome AS habilidade, h.dano, h.custo_mana, h.alcance AS alcance_habilidade, h.tipo AS tipo_habilidade
FROM arena.personagens p
LEFT JOIN arena.habilidades h ON h.personagem_id = p.id
ORDER BY CASE p.tipo
             WHEN 'guerreiro' THEN 1
             WHEN 'mago' THEN 2
             WHEN 'arqueiro' THEN 3
             WHEN 'goblin' THEN 4
             ELSE 5
         END,
         h.id;

-- 8. Demonstração opcional da trigger e das restrições.
-- Execute o bloco todo como script. Os dados criados são removidos pelo ROLLBACK.
BEGIN;
DO $demonstracao$
DECLARE
    v_tag text := substr(md5(clock_timestamp()::text || pg_backend_pid()::text), 1, 10);
    v_jogador bigint;
    v_guerreiro bigint;
    v_mago bigint;
    v_goblin bigint;
    v_partida bigint;
    v_partidas_finalizadas bigint;
    v_vitorias bigint;
    v_derrotas bigint;
    v_mensagem text;
    v_estado text;
    v_rejeitou boolean := false;
BEGIN
    INSERT INTO arena.jogadores (apelido, email, senha_hash)
    VALUES ('demo_' || v_tag, 'demo.' || v_tag || '@example.invalid', 'hash_de_demonstracao')
    RETURNING id INTO v_jogador;

    SELECT id INTO STRICT v_guerreiro FROM arena.personagens WHERE tipo = 'guerreiro';
    SELECT id INTO STRICT v_mago FROM arena.personagens WHERE tipo = 'mago';
    SELECT id INTO STRICT v_goblin FROM arena.personagens WHERE tipo = 'goblin';

    -- Entidade: apelido duplicado deve ser recusado pela constraint UNIQUE.
    v_rejeitou := false;
    BEGIN
        INSERT INTO arena.jogadores (apelido, email, senha_hash)
        VALUES ('demo_' || v_tag, 'duplicado.' || v_tag || '@example.invalid', 'hash_de_demonstracao');
    EXCEPTION WHEN unique_violation THEN
        v_rejeitou := true;
        RAISE NOTICE 'UNIQUE recusou apelido duplicado.';
    END;
    IF NOT v_rejeitou THEN RAISE EXCEPTION 'A constraint UNIQUE não foi aplicada.'; END IF;

    -- Domínio: apelido fora do formato aceito deve ser rejeitado.
    v_rejeitou := false;
    BEGIN
        INSERT INTO arena.jogadores (apelido, email, senha_hash)
        VALUES (' inválido ', 'invalido.' || v_tag || '@example.invalid', 'hash_de_demonstracao');
    EXCEPTION WHEN check_violation THEN
        v_rejeitou := true;
        RAISE NOTICE 'CHECK de domínio recusou apelido inválido.';
    END;
    IF NOT v_rejeitou THEN RAISE EXCEPTION 'A restrição de domínio não foi aplicada.'; END IF;

    -- Referencial: jogador inexistente não pode ser associado a uma partida.
    v_rejeitou := false;
    BEGIN
        INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
        VALUES (-999999, v_guerreiro, v_goblin);
    EXCEPTION WHEN foreign_key_violation THEN
        v_rejeitou := true;
        RAISE NOTICE 'Chave estrangeira recusou jogador inexistente.';
    END;
    IF NOT v_rejeitou THEN RAISE EXCEPTION 'A chave estrangeira não foi aplicada.'; END IF;

    -- Regra de negócio: o inimigo precisa ser Goblin.
    v_rejeitou := false;
    BEGIN
        INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
        VALUES (v_jogador, v_guerreiro, v_mago);
    EXCEPTION WHEN check_violation THEN
        v_rejeitou := true;
        RAISE NOTICE 'Trigger recusou uma classe como inimigo.';
    END;
    IF NOT v_rejeitou THEN RAISE EXCEPTION 'A regra de inimigo Goblin não foi aplicada.'; END IF;

    INSERT INTO arena.partidas (jogador_id, personagem_id, inimigo_id)
    VALUES (v_jogador, v_guerreiro, v_goblin)
    RETURNING id INTO v_partida;

    IF EXISTS (
        SELECT 1 FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador
    ) THEN
        RAISE EXCEPTION 'Partida em andamento não deveria ter estatísticas de partidas finalizadas.';
    END IF;
    RAISE NOTICE 'Validado: partida aberta sem linha de estatísticas; partida_id=%', v_partida;

    UPDATE arena.partidas SET estado = 'vitoria' WHERE id = v_partida;

    SELECT partidas_finalizadas, vitorias, derrotas
      INTO STRICT v_partidas_finalizadas, v_vitorias, v_derrotas
    FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF (v_partidas_finalizadas, v_vitorias, v_derrotas) IS DISTINCT FROM (1, 1, 0) THEN
        RAISE EXCEPTION 'A trigger deveria contabilizar exatamente uma vitória: partidas=%, vitórias=%, derrotas=%.',
            v_partidas_finalizadas, v_vitorias, v_derrotas;
    END IF;
    RAISE NOTICE 'Depois da finalização, vitórias contabilizadas pela trigger=%', v_vitorias;

    v_rejeitou := false;
    BEGIN
        UPDATE arena.partidas SET estado = 'derrota' WHERE id = v_partida;
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS v_mensagem = MESSAGE_TEXT;
        IF v_mensagem NOT LIKE 'Partidas finalizadas sao imutaveis%' THEN
            RAISE;
        END IF;
        v_rejeitou := true;
        RAISE NOTICE 'Alteração posterior recusada: partida finalizada é imutável.';
    END;
    IF NOT v_rejeitou THEN
        RAISE EXCEPTION 'A proteção de partida finalizada não foi aplicada.';
    END IF;

    SELECT estado INTO STRICT v_estado
    FROM arena.partidas WHERE id = v_partida;
    SELECT partidas_finalizadas, vitorias, derrotas
      INTO STRICT v_partidas_finalizadas, v_vitorias, v_derrotas
    FROM arena.estatisticas_jogadores WHERE jogador_id = v_jogador;
    IF v_estado <> 'vitoria'
       OR (v_partidas_finalizadas, v_vitorias, v_derrotas) IS DISTINCT FROM (1, 1, 0) THEN
        RAISE EXCEPTION 'A tentativa recusada alterou o resultado ou duplicou as estatísticas.';
    END IF;
END;
$demonstracao$;
ROLLBACK;

