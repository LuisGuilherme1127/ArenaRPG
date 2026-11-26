# Banco de dados do Arena RPG

Este diretório contém o modelo PostgreSQL da Etapa 15. Ele prepara cadastro, catálogo, histórico, ranking e permissões para uma integração futura. A aplicação Java continua sendo responsável pelas regras de combate; estes scripts não substituem nem executam o motor de partidas.

## Modelo e relacionamentos

- `arena.jogadores` guarda apelido, email normalizado e `senha_hash`. Nunca armazene senha em texto puro.
- `arena.personagens` é o catálogo com os quatro tipos e os atributos atuais do backend.
- `arena.habilidades` guarda as três habilidades especiais ligadas à classe correspondente. O Goblin não possui habilidade especial.
- `arena.partidas` registra jogador, classe jogável, inimigo Goblin, estado, rodadas e datas. É um registro de resultado/histórico; posições e ações detalhadas do tabuleiro não são persistidas nesta etapa.
- `arena.estatisticas_jogadores` mantém totais agregados atualizados pela trigger.
- `arena.vw_ranking_jogadores` expõe apenas identificador público, apelido e estatísticas, sem email ou hash.

Cardinalidades: um jogador pode ter zero ou muitas partidas (1:N); uma classe pode aparecer em muitas partidas como personagem jogável (1:N) e em muitas como inimigo (1:N); um personagem pode possuir zero ou muitas habilidades (1:N); um jogador tem zero ou uma linha de estatísticas (1:0..1). A partida referencia uma classe jogável e um inimigo por chaves estrangeiras separadas.

## Regras e proteção dos dados

- Integridade de entidade: chaves primárias e unicidades em `01_tabelas.sql`.
- Integridade referencial: FKs entre partidas, jogadores, personagens, habilidades e estatísticas.
- Integridade de domínio: tipos PostgreSQL, `NOT NULL`, `DEFAULT` e `CHECK` para valores e domínios conhecidos.
- Integridade definida pelo usuário: a trigger em `03_triggers.sql` exige classe jogável e Goblin, inicia partidas abertas e finaliza a data automaticamente.
- A trigger contabiliza apenas a transição de `em_andamento` para `vitoria` ou `derrota`. Um upsert atômico soma estatísticas em operações concorrentes. Partidas finalizadas são imutáveis e não podem ser excluídas; isso impede recontagem acidental. Correções futuras devem usar procedimento administrativo auditável, ainda não implementado.
- O índice parcial `(jogador_id, finalizada_em DESC)` atende ao histórico recente de partidas finalizadas e não duplica índices de PK/UNIQUE.
- `arena_app` recebe permissões para o uso futuro da API. `arena_consulta` pode ler somente a view de ranking. As roles são criadas sem senha; configure credenciais pelo administrador no DBeaver, fora dos arquivos versionados. Se já existirem, o script reforça os atributos não administrativos e aplica os grants/revokes definidos. O administrador deve revisar associações de roles previamente existentes.

## Ordem de execução no DBeaver

Conecte-se ao banco já existente `arena_rpg` usando uma conta administrativa. Abra cada arquivo e execute-o como SQL Script, na ordem abaixo. Não é necessário executar comandos do terminal `psql`.

1. `01_tabelas.sql` — cria o schema e as tabelas.
2. `02_indices.sql` — cria o índice explícito do histórico.
3. `03_triggers.sql` — cria funções e triggers de validação/contabilização.
4. `04_views.sql` — cria a view de ranking.
5. `05_permissoes.sql` — cria as roles sem senha e aplica grants/revokes.
6. `06_dados_iniciais.sql` — insere personagens e habilidades oficiais de forma repetível.
7. `07_testes.sql` — executa verificações transacionais e termina com `ROLLBACK`.

Os scripts 01–04 usam `IF NOT EXISTS`, `CREATE OR REPLACE` ou substituição explícita das triggers para permitir repetição básica. Isso não é uma ferramenta de migração: se já houver tabelas com estrutura incompatível, revise-as manualmente em vez de supor que a repetição as atualizará. O script de permissões revoga privilégios nas relações do schema antes de conceder os acessos definidos neste projeto.

Conforme informado pelo usuário na Etapa 16A, os scripts 01–07 já foram executados manualmente no banco local. Nenhum deles foi executado pelo agente nesta etapa. O novo script `08_demonstracao.sql` contém consultas de metadados e catálogo que não alteram dados; seu bloco opcional de demonstração usa transação e `ROLLBACK` para mostrar restrições e triggers sem deixar registros permanentes. Ele ainda não foi executado.

## Consultas de exemplo

```sql
SELECT *
FROM arena.vw_ranking_jogadores
ORDER BY vitorias DESC, taxa_vitorias_percentual DESC, apelido ASC;

SELECT id, estado, rodadas, iniciada_em, finalizada_em
FROM arena.partidas
WHERE jogador_id = 1 AND finalizada_em IS NOT NULL
ORDER BY finalizada_em DESC;
```

## Teste manual de concorrência (banco descartável)

O teste concorrente não está automatizado em `07_testes.sql`, pois requer duas conexões simultâneas e uma delas precisa confirmar a transação. Faça-o somente em uma cópia descartável do banco, nunca em dados reais:

1. Crie um jogador de teste e uma partida em andamento pela sessão A; guarde o ID.
2. Na sessão A, atualize essa partida para `vitoria` sem confirmar a transação.
3. Na sessão B, tente atualizar a mesma partida para `derrota`. A sessão B deve aguardar o lock da linha.
4. Confirme a transação da sessão A. A atualização da sessão B deve então falhar porque a partida agora está finalizada.
5. Reverta a transação da sessão B e confira que o jogador recebeu exatamente uma vitória e nenhuma derrota.

As senhas de `arena_app` e `arena_consulta`, caso necessárias no ambiente local, devem ser definidas separadamente por uma conta administrativa no DBeaver. Não registre credenciais neste repositório.

## Demonstração acadêmica (Etapas 16A e 16B)

Conecte-se ao banco `arena_rpg` no DBeaver com uma conta administrativa/owner para que as consultas de metadados, a view e a demonstração transacional estejam disponíveis. Execute `08_demonstracao.sql` como SQL Script para consultar tabelas, constraints, índices, triggers, privilégios efetivos, view e o catálogo com `JOIN`. As consultas de privilégio nomeiam explicitamente `arena_app` e `arena_consulta`, sem depender de o usuário conectado pertencer a essas roles; elas também mostram que `arena_consulta` não lê o hash.

O bloco final cria temporariamente um jogador e uma partida, tenta violar `UNIQUE`, `CHECK`, FK e a regra de que o inimigo deve ser Goblin, valida que partida aberta não tem linha de estatísticas, finaliza como vitória e confere os totais exatos (1 finalizada, 1 vitória, 0 derrotas). Em seguida, testa a imutabilidade, valida a mensagem específica da trigger e confirma que estado e estatísticas não mudaram. O bloco termina com `ROLLBACK`; execute-o inteiro como SQL Script e não confirme a transação com `COMMIT`. Se uma exceção interromper o script antes do `ROLLBACK`, execute `ROLLBACK;` manualmente na mesma sessão antes de fechá-la.

Na auditoria estática da Etapa 16B nenhum script SQL foi executado. A execução demonstrativa pelo usuário no DBeaver continua pendente.

O script é para demonstração, não é migração nem inicialização automática do Spring Boot.

