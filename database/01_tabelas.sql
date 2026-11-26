-- Execute conectado ao banco existente arena_rpg, com um usuario administrador.
-- Este script cria somente o schema do projeto; nao cria nem remove o banco.

CREATE SCHEMA IF NOT EXISTS arena;

CREATE TABLE IF NOT EXISTS arena.jogadores (
    id bigint GENERATED ALWAYS AS IDENTITY,
    apelido varchar(30) NOT NULL,
    email varchar(254) NOT NULL,
    senha_hash varchar(255) NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_jogadores PRIMARY KEY (id),
    CONSTRAINT uq_jogadores_apelido UNIQUE (apelido),
    CONSTRAINT uq_jogadores_email UNIQUE (email),
    CONSTRAINT ck_jogadores_apelido
        CHECK (apelido = btrim(apelido) AND char_length(apelido) BETWEEN 3 AND 30),
    CONSTRAINT ck_jogadores_email
        CHECK (
            email = lower(btrim(email))
            AND email ~ '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$'
        )
);

COMMENT ON COLUMN arena.jogadores.senha_hash IS
    'Armazenar somente hash adaptativo produzido pela aplicacao; nunca senha em texto puro.';

CREATE TABLE IF NOT EXISTS arena.personagens (
    id bigint GENERATED ALWAYS AS IDENTITY,
    tipo varchar(20) NOT NULL,
    nome varchar(60) NOT NULL,
    jogavel boolean NOT NULL,
    vida_maxima integer NOT NULL,
    ataque integer NOT NULL,
    defesa integer NOT NULL,
    mana_maxima integer NOT NULL,
    alcance integer NOT NULL,
    movimento_maximo integer NOT NULL,

    CONSTRAINT pk_personagens PRIMARY KEY (id),
    CONSTRAINT uq_personagens_tipo UNIQUE (tipo),
    CONSTRAINT uq_personagens_nome UNIQUE (nome),
    CONSTRAINT ck_personagens_tipo
        CHECK (tipo IN ('guerreiro', 'mago', 'arqueiro', 'goblin')),
    CONSTRAINT ck_personagens_nome CHECK (char_length(btrim(nome)) > 0),
    CONSTRAINT ck_personagens_vida CHECK (vida_maxima > 0),
    CONSTRAINT ck_personagens_ataque CHECK (ataque >= 0),
    CONSTRAINT ck_personagens_defesa CHECK (defesa >= 0),
    CONSTRAINT ck_personagens_mana CHECK (mana_maxima >= 0),
    CONSTRAINT ck_personagens_alcance CHECK (alcance > 0),
    CONSTRAINT ck_personagens_movimento CHECK (movimento_maximo >= 0),
    CONSTRAINT ck_personagens_jogavel_por_tipo
        CHECK ((tipo = 'goblin' AND jogavel = false) OR (tipo <> 'goblin' AND jogavel = true))
);

CREATE TABLE IF NOT EXISTS arena.habilidades (
    id bigint GENERATED ALWAYS AS IDENTITY,
    personagem_id bigint NOT NULL,
    nome varchar(80) NOT NULL,
    descricao varchar(300) NOT NULL,
    dano integer NOT NULL,
    custo_mana integer NOT NULL,
    alcance integer NOT NULL,
    tipo varchar(20) NOT NULL,

    CONSTRAINT pk_habilidades PRIMARY KEY (id),
    CONSTRAINT fk_habilidades_personagem
        FOREIGN KEY (personagem_id) REFERENCES arena.personagens (id) ON DELETE RESTRICT,
    CONSTRAINT uq_habilidades_personagem_nome UNIQUE (personagem_id, nome),
    CONSTRAINT ck_habilidades_nome CHECK (char_length(btrim(nome)) > 0),
    CONSTRAINT ck_habilidades_descricao CHECK (char_length(btrim(descricao)) > 0),
    CONSTRAINT ck_habilidades_dano CHECK (dano >= 0),
    CONSTRAINT ck_habilidades_custo_mana CHECK (custo_mana >= 0),
    CONSTRAINT ck_habilidades_alcance CHECK (alcance > 0),
    CONSTRAINT ck_habilidades_tipo CHECK (tipo IN ('FISICA', 'MAGICA', 'DISTANCIA'))
);

CREATE TABLE IF NOT EXISTS arena.partidas (
    id bigint GENERATED ALWAYS AS IDENTITY,
    jogador_id bigint NOT NULL,
    personagem_id bigint NOT NULL,
    inimigo_id bigint NOT NULL,
    estado varchar(20) NOT NULL DEFAULT 'em_andamento',
    rodadas integer NOT NULL DEFAULT 1,
    iniciada_em timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalizada_em timestamptz,

    CONSTRAINT pk_partidas PRIMARY KEY (id),
    CONSTRAINT fk_partidas_jogador
        FOREIGN KEY (jogador_id) REFERENCES arena.jogadores (id) ON DELETE RESTRICT,
    CONSTRAINT fk_partidas_personagem
        FOREIGN KEY (personagem_id) REFERENCES arena.personagens (id) ON DELETE RESTRICT,
    CONSTRAINT fk_partidas_inimigo
        FOREIGN KEY (inimigo_id) REFERENCES arena.personagens (id) ON DELETE RESTRICT,
    CONSTRAINT ck_partidas_personagens_distintos CHECK (personagem_id <> inimigo_id),
    CONSTRAINT ck_partidas_estado CHECK (estado IN ('em_andamento', 'vitoria', 'derrota')),
    CONSTRAINT ck_partidas_rodadas CHECK (rodadas >= 1),
    CONSTRAINT ck_partidas_datas_estado CHECK (
        (estado = 'em_andamento' AND finalizada_em IS NULL)
        OR (estado IN ('vitoria', 'derrota') AND finalizada_em IS NOT NULL)
    ),
    CONSTRAINT ck_partidas_ordem_datas CHECK (finalizada_em IS NULL OR finalizada_em >= iniciada_em)
);

CREATE TABLE IF NOT EXISTS arena.estatisticas_jogadores (
    jogador_id bigint NOT NULL,
    partidas_finalizadas bigint NOT NULL DEFAULT 0,
    vitorias bigint NOT NULL DEFAULT 0,
    derrotas bigint NOT NULL DEFAULT 0,
    atualizado_em timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_estatisticas_jogadores PRIMARY KEY (jogador_id),
    CONSTRAINT fk_estatisticas_jogador
        FOREIGN KEY (jogador_id) REFERENCES arena.jogadores (id) ON DELETE CASCADE,
    CONSTRAINT ck_estatisticas_nao_negativas
        CHECK (partidas_finalizadas >= 0 AND vitorias >= 0 AND derrotas >= 0),
    CONSTRAINT ck_estatisticas_soma
        CHECK (partidas_finalizadas = vitorias + derrotas)
);

COMMENT ON TABLE arena.partidas IS
    'Resultado historico relativo ao jogador: vitoria ou derrota; partidas finalizadas sao imutaveis.';
COMMENT ON COLUMN arena.partidas.estado IS
    'Valores alinhados a EstadoPartida: em_andamento, vitoria e derrota.';
COMMENT ON COLUMN arena.habilidades.tipo IS
    'Valores alinhados aos nomes do enum Java TipoHabilidade.';
