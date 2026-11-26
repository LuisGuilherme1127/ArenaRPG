# ARENA RPG — ETAPA 16A: INTEGRAÇÃO INICIAL COM POSTGRESQL

## Contexto

O projeto Arena RPG já possui:

- Frontend JavaScript com Vite.
- Backend Java 21 com Spring Boot e Maven.
- Motor de combate baseado em POO.
- API REST funcional.
- Integração frontend/backend.
- Repositório publicado no GitHub.
- PostgreSQL instalado localmente.
- Banco arena_rpg na porta 5432.
- Schema arena criado no PostgreSQL.
- Cinco tabelas criadas.
- Scripts SQL das etapas anteriores executados pelo usuário no DBeaver.
- Dados iniciais de personagens e habilidades inseridos.

A prioridade acadêmica atual é apresentar o banco de dados ao professor, demonstrando as estruturas SQL e sua conexão com o sistema.

Não precisamos implementar todas as funcionalidades de persistência neste momento.

## 1. Diagnóstico

Analise:

- Backend existente.
- pom.xml.
- Classes de domínio.
- Controllers e services.
- Scripts da pasta database.
- Documentação existente.
- Permissões concedidas ao papel arena_app.

Não presuma nomes de colunas ou tipos de dados sem consultar os scripts SQL.

## 2. Dependências

Adicione as dependências compatíveis com a versão atual do Spring Boot:

- Spring Boot Starter JDBC.
- PostgreSQL JDBC Driver.

Nesta etapa, priorize Spring JDBC e JdbcTemplate.

Não introduza JPA ou Hibernate sem necessidade.

Preserve Java 21 e Maven Wrapper.

## 3. Configuração do banco

Configure um perfil Spring chamado postgres para conexão com:

- Host: localhost.
- Porta: 5432.
- Database: arena_rpg.
- Schema: arena.
- Usuário: arena_app.

Utilize variáveis de ambiente:

DB_URL
DB_USERNAME
DB_PASSWORD

Não inclua senhas reais em arquivos versionados.

Preserve o funcionamento do perfil padrão sem PostgreSQL, para permitir a execução dos testes existentes e a utilização anterior do sistema.

Não execute scripts SQL automaticamente.

Não recrie, altere ou remova tabelas.

## 4. Verificação da conexão

Implemente o endpoint:

GET /api/status/database

No perfil postgres, utilize uma consulta SQL real, como SELECT 1, para verificar a conexão.

Retorne HTTP 200 quando o banco estiver disponível e HTTP 503 quando estiver indisponível, caso a aplicação esteja em execução.

Quando o perfil postgres estiver desativado, informe claramente que a integração está desabilitada, sem simular uma conexão bem-sucedida.

Não exponha credenciais ou informações internas sensíveis.

## 5. Consulta de personagens e habilidades

Implemente uma camada de repository ou DAO utilizando JdbcTemplate.

Consulte os dados das tabelas:

- arena.personagens
- arena.habilidades

Utilize consultas parametrizadas quando houver parâmetros externos.

Implemente consultas que permitam demonstrar SELECT e JOIN entre essas tabelas.

No perfil postgres, integre os dados aos endpoints existentes:

GET /api/personagens
GET /api/personagens/{tipo}

Preserve o contrato JSON existente e o comportamento do frontend.

As classes de domínio Java devem continuar responsáveis pelas regras de combate.

Verifique a consistência entre os atributos SQL e os atributos utilizados pelo motor de combate. Caso existam divergências, informe e não altere arbitrariamente as regras.

## 6. Preparação acadêmica

Crie um arquivo database/08_demonstracao.sql contendo consultas não destrutivas para apresentar:

- Tabelas existentes.
- Chaves primárias.
- Chaves estrangeiras.
- Constraints.
- Índices.
- Triggers.
- Contas e permissões.
- View de ranking.
- Personagens e habilidades.
- Relacionamentos utilizando JOIN.

Prepare também um roteiro de demonstração das regras de integridade e triggers, utilizando transações e ROLLBACK quando apropriado.

Não insira jogadores ou partidas fictícias permanentemente.

## 7. Testes

Preserve todos os testes existentes.

Adicione testes para a camada JDBC e os novos endpoints.

Separe testes unitários de testes que exigem PostgreSQL.

Não utilize credenciais reais sem minha configuração manual.

Não declare testes de integração como aprovados se não foram executados.

## 8. Documentação

Crie docs/integracao-postgresql.md.

Documente:

- Arquitetura.
- Dependências.
- Funcionamento do JDBC e JdbcTemplate.
- Consultas SQL utilizadas.
- Variáveis de ambiente.
- Comandos PowerShell.
- Ativação do perfil postgres.
- Endpoints.
- Testes.
- Limitações.

Registre este prompt integralmente em docs/prompts/etapa-16a.md.

Atualize o README principal conforme as funcionalidades efetivamente implementadas.

## 9. Restrições

- Não modificar o frontend.
- Não alterar regras de combate.
- Não implementar cadastro ou autenticação.
- Não implementar persistência completa de partidas.
- Não modificar estruturas SQL existentes sem autorização.
- Não substituir triggers por lógica Java.
- Não incluir credenciais reais.
- Não realizar commit ou push automaticamente.

## 10. Relatório final

Informe:

1. Diagnóstico.
2. Dependências adicionadas.
3. Arquivos criados e modificados.
4. Consultas SQL implementadas.
5. Endpoints disponíveis.
6. Testes executados e resultados.
7. Comandos exatos para iniciar o backend conectado ao PostgreSQL.
8. Limitações e pendências.

Implemente a Etapa 16A respeitando a prioridade acadêmica atual.