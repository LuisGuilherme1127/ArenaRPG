CREATE OR REPLACE FUNCTION arena.validar_ciclo_partida()
RETURNS trigger
LANGUAGE plpgsql
SET search_path = pg_catalog, arena
AS $func$
DECLARE
    v_personagem_jogavel boolean;
    v_tipo_inimigo text;
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.estado <> 'em_andamento' OR NEW.finalizada_em IS NOT NULL THEN
            RAISE EXCEPTION USING
                ERRCODE = '23514',
                MESSAGE = 'Uma partida deve ser criada em andamento e sem data de finalizacao.';
        END IF;

        SELECT jogavel
          INTO v_personagem_jogavel
          FROM arena.personagens
         WHERE id = NEW.personagem_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION USING ERRCODE = '23503', MESSAGE = 'Personagem jogavel inexistente.';
        END IF;
        IF v_personagem_jogavel IS DISTINCT FROM true THEN
            RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'A partida precisa usar uma classe jogavel.';
        END IF;

        SELECT tipo
          INTO v_tipo_inimigo
          FROM arena.personagens
         WHERE id = NEW.inimigo_id;
        IF NOT FOUND THEN
            RAISE EXCEPTION USING ERRCODE = '23503', MESSAGE = 'Personagem inimigo inexistente.';
        END IF;
        IF v_tipo_inimigo <> 'goblin' THEN
            RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'O inimigo inicial deve ser Goblin.';
        END IF;

        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.estado <> 'em_andamento' THEN
            RAISE EXCEPTION USING
                ERRCODE = '23514',
                MESSAGE = 'Partidas finalizadas sao imutaveis; corrija erros por procedimento administrativo auditado.';
        END IF;

        IF ROW(NEW.id, NEW.jogador_id, NEW.personagem_id, NEW.inimigo_id, NEW.iniciada_em)
           IS DISTINCT FROM
           ROW(OLD.id, OLD.jogador_id, OLD.personagem_id, OLD.inimigo_id, OLD.iniciada_em) THEN
            RAISE EXCEPTION USING
                ERRCODE = '23514',
                MESSAGE = 'Identidade, participante e personagens de uma partida nao podem ser alterados.';
        END IF;

        IF NEW.estado = 'em_andamento' THEN
            IF NEW.finalizada_em IS NOT NULL THEN
                RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'Partida em andamento nao pode ter data de finalizacao.';
            END IF;
            RETURN NEW;
        ELSIF NEW.estado IN ('vitoria', 'derrota') THEN
            NEW.finalizada_em := clock_timestamp();
            RETURN NEW;
        ELSE
            RAISE EXCEPTION USING ERRCODE = '23514', MESSAGE = 'Estado de partida invalido.';
        END IF;
    ELSIF TG_OP = 'DELETE' THEN
        IF OLD.estado <> 'em_andamento' THEN
            RAISE EXCEPTION USING
                ERRCODE = '23514',
                MESSAGE = 'Partidas finalizadas nao podem ser excluidas.';
        END IF;
        RETURN OLD;
    END IF;

    RAISE EXCEPTION 'Operacao inesperada % no ciclo de partidas.', TG_OP;
END;
$func$;

CREATE OR REPLACE FUNCTION arena.contabilizar_partida_finalizada()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = pg_catalog, arena
AS $func$
BEGIN
    IF TG_OP = 'UPDATE'
       AND OLD.estado = 'em_andamento'
       AND NEW.estado IN ('vitoria', 'derrota') THEN
        INSERT INTO arena.estatisticas_jogadores (
            jogador_id,
            partidas_finalizadas,
            vitorias,
            derrotas,
            atualizado_em
        )
        VALUES (
            NEW.jogador_id,
            1,
            CASE WHEN NEW.estado = 'vitoria' THEN 1 ELSE 0 END,
            CASE WHEN NEW.estado = 'derrota' THEN 1 ELSE 0 END,
            clock_timestamp()
        )
        ON CONFLICT (jogador_id) DO UPDATE
        SET partidas_finalizadas = arena.estatisticas_jogadores.partidas_finalizadas
                                  + EXCLUDED.partidas_finalizadas,
            vitorias = arena.estatisticas_jogadores.vitorias + EXCLUDED.vitorias,
            derrotas = arena.estatisticas_jogadores.derrotas + EXCLUDED.derrotas,
            atualizado_em = EXCLUDED.atualizado_em;
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$func$;

DROP TRIGGER IF EXISTS trg_partidas_validar_ciclo ON arena.partidas;
CREATE TRIGGER trg_partidas_validar_ciclo
    BEFORE INSERT OR UPDATE OR DELETE ON arena.partidas
    FOR EACH ROW
    EXECUTE FUNCTION arena.validar_ciclo_partida();

DROP TRIGGER IF EXISTS trg_partidas_contabilizar_finalizacao ON arena.partidas;
CREATE TRIGGER trg_partidas_contabilizar_finalizacao
    AFTER INSERT OR UPDATE OR DELETE ON arena.partidas
    FOR EACH ROW
    EXECUTE FUNCTION arena.contabilizar_partida_finalizada();
