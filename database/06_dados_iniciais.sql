-- Catalogo fiel aos construtores Java atuais. Nao cria jogadores ou partidas ficticias.
BEGIN;

INSERT INTO arena.personagens (
    tipo, nome, jogavel, vida_maxima, ataque, defesa, mana_maxima, alcance, movimento_maximo
)
VALUES
    ('guerreiro', 'Guerreiro', true, 120, 20, 0, 30, 1, 3),
    ('mago', 'Mago', true, 80, 15, 0, 100, 3, 3),
    ('arqueiro', 'Arqueiro', true, 90, 18, 0, 50, 4, 3),
    ('goblin', 'Goblin', false, 100, 12, 0, 0, 1, 2)
ON CONFLICT (tipo) DO NOTHING;

INSERT INTO arena.habilidades (personagem_id, nome, descricao, dano, custo_mana, alcance, tipo)
SELECT p.id, dados.nome, dados.descricao, dados.dano, dados.custo_mana, dados.alcance, dados.tipo
FROM (VALUES
    ('guerreiro', 'Golpe Poderoso', 'Um golpe corpo a corpo de grande força.', 35, 10, 1, 'FISICA'),
    ('mago', 'Bola de Fogo', 'Uma esfera de fogo que atinge um alvo à distância.', 30, 20, 3, 'MAGICA'),
    ('arqueiro', 'Flecha Precisa', 'Um disparo preciso que alcança alvos distantes.', 28, 15, 4, 'DISTANCIA')
) AS dados(tipo_personagem, nome, descricao, dano, custo_mana, alcance, tipo)
JOIN arena.personagens AS p ON p.tipo = dados.tipo_personagem
ON CONFLICT (personagem_id, nome) DO NOTHING;

COMMIT;

