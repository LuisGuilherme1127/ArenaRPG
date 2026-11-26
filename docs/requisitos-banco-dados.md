# Requisitos e modelo de Banco de Dados — Arena RPG

## Contexto

O Arena RPG é um projeto acadêmico de RPG tático por turnos. O frontend usa JavaScript puro com Vite, e o backend Java 21 com Spring Boot concentra personagens, habilidades e regras de combate. Nesta Etapa 15 foi preparado o modelo PostgreSQL, sem conexão da aplicação ao banco. As partidas continuam em memória no backend até uma etapa de integração futura.

## Modelo relacional implementado

O schema PostgreSQL é `arena`. As tabelas são:

| Tabela | Responsabilidade |
| --- | --- |
| `jogadores` | Apelido público, email normalizado, hash de senha e data de criação. |
| `personagens` | Catálogo de Guerreiro, Mago, Arqueiro e Goblin, com atributos do backend Java. |
| `habilidades` | Habilidades especiais e seus efeitos declarados: dano, custo de mana, alcance e tipo. |
| `partidas` | Jogador, classe selecionada, Goblin adversário, estado, rodadas e datas de início/finalização. |
| `estatisticas_jogadores` | Totais agregados de partidas concluídas, vitórias e derrotas. |

A view `arena.vw_ranking_jogadores` publica identificador, apelido, totais e taxa de vitória, sem email ou hash de senha.

Relacionamentos e cardinalidades:

- `jogadores` 1:N `partidas`: cada partida pertence a um jogador; um jogador pode ter várias partidas.
- `personagens` 1:N `habilidades`: uma classe pode possuir várias habilidades; o Goblin não tem habilidade especial cadastrada.
- `personagens` 1:N `partidas` pelos papéis `personagem_id` (classe jogável) e `inimigo_id` (adversário).
- `jogadores` 1:0..1 `estatisticas_jogadores`: estatísticas são criadas quando a primeira partida termina.

A tabela de partidas guarda o resultado e o histórico mínimo, não o estado tático completo do tabuleiro nem cada ação. Isso evita duplicar o motor Java; persistir partidas em andamento e eventos detalhados pode ser decidido durante a integração.

### Atributos alinhados ao backend

| Tipo | Jogável | Vida | Ataque | Defesa | Mana | Alcance | Movimento |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Guerreiro | Sim | 120 | 20 | 0 | 30 | 1 | 3 |
| Mago | Sim | 80 | 15 | 0 | 100 | 3 | 3 |
| Arqueiro | Sim | 90 | 18 | 0 | 50 | 4 | 3 |
| Goblin | Não | 100 | 12 | 0 | 0 | 1 | 2 |

Habilidades: Golpe Poderoso (35 dano, 10 mana, alcance 1, `FISICA`); Bola de Fogo (30, 20, 3, `MAGICA`); Flecha Precisa (28, 15, 4, `DISTANCIA`). Defesa é mantida como atributo, mas não é usada por estes scripts para reduzir dano. As regras efetivas continuam no backend.

## Requisitos acadêmicos e rastreabilidade

Cada critério do professor vale um ponto.

1. **Tabelas e quatro integridades** — `database/01_tabelas.sql`: entidade por PK e UNIQUE; referencial por FK; domínio por tipos, `NOT NULL`, `DEFAULT` e `CHECK`; regras de usuário por checks semânticos e validação PL/pgSQL da classe jogável, Goblin e ciclo da partida em `03_triggers.sql`.
2. **Trigger PL/pgSQL** — `database/03_triggers.sql`: `validar_ciclo_partida()` valida transições e datas. `contabilizar_partida_finalizada()` incrementa estatísticas somente na transição de partida aberta para vitória/derrota. O upsert é atômico para partidas diferentes do mesmo jogador. Partidas concluídas tornam-se imutáveis, evitando recontagem por alteração/exclusão. Uma correção administrativa auditável ainda precisa ser desenhada.
3. **Índice explícito** — `database/02_indices.sql`: índice parcial `idx_partidas_historico_jogador` em `(jogador_id, finalizada_em DESC)` para o histórico individual recente; não repete PK nem UNIQUE.
4. **Contas e permissões** — `database/05_permissoes.sql`: `arena_app` recebe permissões restritas para a futura aplicação; `arena_consulta` tem leitura apenas da view. Nenhuma senha é definida ou guardada pelos scripts. O script reforça atributos não administrativos e aplica grants/revokes sobre objetos no schema; o administrador deve revisar associações de roles que já existam. Atribuição de credenciais é tarefa administrativa local.
5. **View** — `database/04_views.sql`: `arena.vw_ranking_jogadores`, com divisão por zero tratada por `NULLIF` e estatísticas ausentes convertidas para zero. Exemplo: `SELECT * FROM arena.vw_ranking_jogadores ORDER BY vitorias DESC, taxa_vitorias_percentual DESC, apelido ASC;`.

## Execução no DBeaver

O banco `arena_rpg` já existe. Conecte-se a ele no DBeaver com uma conta administrativa; abra e execute cada arquivo como SQL Script, nesta ordem:

1. `database/01_tabelas.sql`
2. `database/02_indices.sql`
3. `database/03_triggers.sql`
4. `database/04_views.sql`
5. `database/05_permissoes.sql`
6. `database/06_dados_iniciais.sql`
7. `database/07_testes.sql`

Os scripts são SQL PostgreSQL/PLpgSQL, sem comandos exclusivos do terminal `psql`. `07_testes.sql` abre uma transação e termina em `ROLLBACK`, removendo os dados de teste. Para verificar concorrência manualmente, siga as instruções do `database/README.md` em um banco descartável com duas sessões; o teste concorrente não foi executado nesta etapa.

Scripts DDL idempotentes não fazem migração automática de uma tabela preexistente cuja estrutura seja diferente. Revise o schema em caso de objetos antigos. O script de permissões não define credenciais e pode alterar os privilégios existentes nas relações do schema `arena`; execute-o somente após revisar que essas permissões são as esperadas para o projeto.

## Testes e escopo desta etapa

Foram preparados testes SQL para catálogo/atributos/habilidades, unicidade, domínio, FK, regra de personagem jogável, não contabilização de partidas abertas, contagem de vitória/derrota, imutabilidade após finalização, remoção de partida aberta sem efeito estatístico, view, índice e grants. **Eles foram escritos, mas não executados.** Nenhuma conexão foi feita ao PostgreSQL e nenhum script foi aplicado ao banco local, conforme a restrição desta etapa.

Não foram alterados backend, frontend ou configuração Maven. Persistência Java, autenticação, hash concreto adotado pela aplicação, isolamento de linhas por usuário, migrações e armazenamento do tabuleiro/eventos ficam para etapas futuras. Sem Row Level Security, o controle de qual jogador pode acessar cada linha precisa ser garantido pela aplicação quando a integração for feita.

## Registros de uso de IA

- Ferramenta: OpenAI Codex, agente de desenvolvimento.
- Modelo registrado nas etapas anteriores: GPT-6; variante exata não aparece para preenchimento confiável nesta sessão.
- Prompt integral da Etapa 14A: [docs/prompts/etapa-14.md](prompts/etapa-14.md).
- Prompt integral da Etapa 15: [docs/prompts/etapa-15.md](prompts/etapa-15.md).
- A modelagem desta etapa foi feita a partir do prompt da Etapa 15 e da inspeção do backend e da documentação do projeto.

### Auditoria da Etapa 16B

- Revisão estática dos scripts `database/01_tabelas.sql` a `database/08_demonstracao.sql`, sem conexão ou execução no PostgreSQL.
- `08_demonstracao.sql` agora confirma a ausência de estatísticas enquanto a partida está em andamento e exige exatamente uma partida finalizada, uma vitória e zero derrotas após a trigger.
- Antes do teste de imutabilidade, `v_rejeitou` é reinicializada; a demonstração também valida que o erro capturado é especificamente o da trigger e que estado e estatísticas permanecem inalterados.
- `07_testes.sql` verifica os erros esperados e confirma que atualização/exclusão recusadas não alteram o resultado nem duplicam as estatísticas.
- As consultas de privilégios em `08_demonstracao.sql` verificam explicitamente os privilégios efetivos de ambas as roles, independentemente das associações do usuário conectado.
- Não foram executados testes SQL nesta auditoria; a execução no DBeaver continua pendente do usuário.
- Ferramenta: OpenAI Codex. O identificador exato do modelo desta execução não foi disponibilizado no contexto; não foi inferido.
- Prompt integral: [docs/prompts/etapa-16b.md](prompts/etapa-16b.md).

