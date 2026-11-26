-- Execute conectado ao banco arena_rpg com uma conta administrativa.
-- As roles ficam sem senha definida. Configure credenciais localmente pelo DBeaver.

DO $roles$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = 'arena_app') THEN
        EXECUTE 'CREATE ROLE arena_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = 'arena_consulta') THEN
        EXECUTE 'CREATE ROLE arena_consulta LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT';
    END IF;
END;
$roles$;

-- Reforce os atributos não administrativos mesmo se as roles já existirem.
ALTER ROLE arena_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT;
ALTER ROLE arena_consulta LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS NOINHERIT;

DO $connect$
BEGIN
    EXECUTE format('GRANT CONNECT ON DATABASE %I TO arena_app, arena_consulta', current_database());
END;
$connect$;

REVOKE CREATE ON SCHEMA arena FROM PUBLIC;
REVOKE ALL ON SCHEMA arena FROM arena_app, arena_consulta;
GRANT USAGE ON SCHEMA arena TO arena_app, arena_consulta;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA arena FROM PUBLIC, arena_app, arena_consulta;
REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA arena FROM PUBLIC, arena_app, arena_consulta;
REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA arena FROM PUBLIC, arena_app, arena_consulta;

-- A futura API precisa dos hashes para autenticar, mas nunca recebe senhas em texto puro.
GRANT SELECT ON arena.jogadores TO arena_app;
GRANT INSERT (apelido, email, senha_hash) ON arena.jogadores TO arena_app;
GRANT UPDATE (apelido, email, senha_hash) ON arena.jogadores TO arena_app;

GRANT SELECT ON arena.personagens, arena.habilidades, arena.estatisticas_jogadores TO arena_app;
GRANT SELECT ON arena.partidas TO arena_app;
GRANT INSERT (jogador_id, personagem_id, inimigo_id) ON arena.partidas TO arena_app;
GRANT UPDATE (estado, rodadas) ON arena.partidas TO arena_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA arena TO arena_app;

-- Consultas externas só veem a projeção pública de ranking.
GRANT SELECT ON arena.vw_ranking_jogadores TO arena_consulta;

-- Funções de trigger não são uma API pública invocável pelas roles da aplicação.
REVOKE EXECUTE ON FUNCTION arena.validar_ciclo_partida() FROM PUBLIC, arena_app, arena_consulta;
REVOKE EXECUTE ON FUNCTION arena.contabilizar_partida_finalizada() FROM PUBLIC, arena_app, arena_consulta;

