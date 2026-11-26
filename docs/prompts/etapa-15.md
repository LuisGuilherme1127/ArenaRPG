# Arena RPG — Etapa 15: Modelagem e implementação da estrutura PostgreSQL

Vamos iniciar a Etapa 15 do projeto Arena RPG.

## Contexto atual

- Sistema acadêmico desenvolvido para as disciplinas de Programação Orientada a Objetos e Banco de Dados.
- Frontend JavaScript puro com Vite.
- Backend Java 21 com Spring Boot e Maven.
- API REST de personagens e partidas implementada.
- Frontend integrado ao backend.
- Regras de combate, turnos, habilidades e comportamento do Goblin centralizados no Java.
- Partidas atualmente armazenadas em memória.
- Ambiente Windows 11, VS Code e PowerShell.
- PostgreSQL 19 instalado localmente na porta 5432.
- DBeaver 26.2.5 instalado e funcionando.
- Banco de dados arena_rpg já criado.
- Conexão administrativa com o banco validada através de SELECT current_database().

## Objetivo da etapa

Projetar e implementar os scripts SQL da estrutura relacional do Arena RPG, atendendo integralmente aos requisitos acadêmicos do professor de Banco de Dados.

Nesta etapa, não integrar o PostgreSQL ao Spring Boot. Essa integração será realizada posteriormente.

## 1. Diagnóstico inicial

Antes de alterar arquivos:

- Analise a estrutura atual do backend.
- Identifique as classes, atributos, enums, habilidades e estados das partidas.
- Analise os DTOs e endpoints existentes.
- Leia os documentos acadêmicos e técnicos presentes em docs.
- Verifique os requisitos registrados em docs/requisitos-banco-dados.md.
- Identifique possíveis incompatibilidades entre o modelo Java e o modelo relacional proposto.

Preserve os nomes, atributos, valores e regras existentes sempre que possível.

## 2. Modelagem relacional

Utilize como proposta inicial as seguintes tabelas:

- jogadores;
- personagens;
- habilidades;
- partidas;
- estatisticas_jogadores.

Adapte a estrutura caso a análise do backend demonstre a necessidade de outras tabelas ou relacionamentos. Justifique as alterações.

A estrutura deve permitir futuramente:

- Cadastro de jogadores.
- Autenticação segura.
- Consulta de personagens e habilidades.
- Registro de partidas.
- Identificação da classe jogável e do inimigo.
- Registro de vitória e derrota.
- Histórico individual.
- Ranking de jogadores.

Não armazenar senhas em texto puro.

Os atributos e valores dos personagens devem corresponder exatamente aos definidos no backend Java atual.

Não duplicar desnecessariamente regras de combate que pertencem ao Java.

## 3. Requisitos acadêmicos obrigatórios

O professor avaliará cinco critérios, valendo um ponto cada.

### 3.1. Criação de tabelas DDL contemplando as quatro integridades

Demonstrar explicitamente:

- Integridade de entidade.
- Integridade referencial.
- Integridade de domínio.
- Integridade definida pelo usuário.

Utilizar adequadamente PRIMARY KEY, FOREIGN KEY, UNIQUE, NOT NULL, CHECK, DEFAULT e tipos de dados PostgreSQL.

Documentar quais trechos SQL atendem a cada integridade.

### 3.2. Trigger

Criar uma função PL/pgSQL com RETURNS TRIGGER e uma trigger que atualize automaticamente as estatísticas do jogador quando uma partida for finalizada.

Garantir que:

- Partidas em andamento não sejam contabilizadas.
- Uma partida finalizada não seja contabilizada duas vezes.
- Vitórias e derrotas sejam contabilizadas corretamente.
- Inserções ou alterações inválidas não comprometam as estatísticas.
- A estratégia para alterações e exclusões de partidas finalizadas seja explicitamente definida e testada.
- A atualização das estatísticas seja segura em operações concorrentes.

Utilizar NEW, OLD e TG_OP quando aplicável.

### 3.3. Campo indexado

Criar pelo menos um índice explícito e útil para as consultas ao histórico de partidas.

Preferencialmente utilizar jogador_id ou um índice composto adequado às consultas previstas.

Explicar a finalidade do índice e evitar índices redundantes com chaves primárias ou constraints UNIQUE.

### 3.4. Contas e permissões

Criar papéis PostgreSQL para:

- arena_app: acesso restrito necessário para a futura aplicação Java.
- arena_consulta: acesso somente às consultas autorizadas, especialmente ao ranking.

Aplicar o princípio do menor privilégio.

Utilizar CREATE ROLE, GRANT e REVOKE quando necessário.

Considerar permissões de conexão, schema, tabelas, views, sequences e funções.

Não conceder SUPERUSER, privilégios administrativos ou permissões desnecessárias.

Garantir que arena_consulta não consiga consultar senhas ou hashes de senhas.

Não colocar senhas reais no código, scripts versionados ou documentação.

Caso seja necessário definir senhas, fornecer instruções separadas para configuração local pelo administrador.

### 3.5. View

Criar uma View chamada vw_ranking_jogadores contendo, quando compatível com a modelagem:

- Identificação pública do jogador.
- Quantidade de partidas finalizadas.
- Vitórias.
- Derrotas.
- Taxa percentual de vitórias.

Tratar divisão por zero.

Não expor informações sensíveis.

Documentar uma consulta SELECT com ORDER BY para exibir o ranking.

## 4. Organização dos arquivos

Criar uma pasta database na raiz do projeto.

Organizar os arquivos da seguinte maneira:

database/
- 01_tabelas.sql
- 02_indices.sql
- 03_triggers.sql
- 04_views.sql
- 05_permissoes.sql
- 06_dados_iniciais.sql
- 07_testes.sql
- README.md

Ajustar a ordem ou organização caso exista uma dependência técnica, documentando o motivo.

Os scripts devem utilizar PostgreSQL e PL/pgSQL nativos, sendo executáveis pelo DBeaver.

Não utilizar comandos exclusivos do terminal psql dentro dos scripts destinados ao editor SQL do DBeaver.

O banco arena_rpg já existe. Não recriar nem excluir o banco.

Não executar DROP DATABASE, DROP SCHEMA CASCADE ou outras operações destrutivas.

## 5. Dados iniciais

Preparar os INSERTs necessários para os personagens Guerreiro, Mago, Arqueiro e Goblin, bem como suas habilidades.

Utilizar os atributos reais presentes nas classes Java.

Não inventar novos atributos ou alterar o balanceamento existente.

Não cadastrar usuários fictícios permanentes ou partidas fictícias nos dados iniciais de produção.

## 6. Testes SQL

Preparar testes para validar:

- Criação das tabelas.
- Integridade de entidade.
- Integridade referencial.
- Integridade de domínio.
- Integridade definida pelo usuário.
- Funcionamento da trigger.
- Contabilização de vitórias e derrotas.
- Prevenção de contabilização duplicada.
- Existência dos índices.
- Funcionamento da View.
- Permissões dos papéis criados.

Os testes que inserem dados temporários devem utilizar transações e ROLLBACK quando apropriado, evitando poluir o banco.

Separar os testes que exigem conexões com usuários distintos.

Não afirmar que um teste foi executado se ele apenas foi escrito.

## 7. Documentação acadêmica

Atualizar docs/requisitos-banco-dados.md com:

- Breve introdução do projeto.
- Modelo relacional.
- Descrição das tabelas.
- Relacionamentos e cardinalidades.
- Mapeamento dos cinco critérios do professor.
- Identificação dos trechos SQL que atendem a cada critério.
- Instruções detalhadas de execução no DBeaver.

Registrar este prompt integral em docs/prompts/etapa-15.md.

Registrar o uso do OpenAI Codex e o modelo efetivamente utilizado, caso essa informação esteja disponível. Se não estiver disponível, deixar indicado que deverá ser preenchida manualmente, sem inventar a identificação do modelo.

Preservar os registros de IA das etapas anteriores.

## 8. Restrições

- Não alterar o frontend.
- Não alterar as regras de combate.
- Não modificar o backend Java nesta etapa.
- Não adicionar JPA, Hibernate ou Flyway ainda.
- Não configurar autenticação no Java.
- Não alterar o pom.xml.
- Não excluir arquivos existentes.
- Não solicitar ou armazenar minhas credenciais reais.
- Não executar os scripts no banco local sem minha autorização.
- Não avançar para a integração Java/PostgreSQL.

## 9. Relatório final

Ao concluir, apresente:

1. Diagnóstico do modelo Java atual.
2. Modelo relacional definitivo e justificativas.
3. Arquivos criados e modificados.
4. Explicação das tabelas e relacionamentos.
5. Mapeamento dos cinco critérios acadêmicos.
6. Explicação da trigger, índice, permissões e View.
7. Ordem exata de execução dos scripts no DBeaver.
8. Testes preparados e testes efetivamente executados.
9. Possíveis problemas e pendências.

Caso encontre um impedimento técnico real, informe antes de realizar alterações incompatíveis.

Implemente somente a Etapa 15.