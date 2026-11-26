# Arena RPG

Arena RPG é um RPG tático 2D de fantasia com batalhas por turnos em um tabuleiro 8×8. O projeto separa uma interface web em JavaScript de uma API em Java; o PostgreSQL está sendo preparado para uma etapa futura.

## Objetivos acadêmicos

- **Programação Orientada a Objetos:** demonstrar abstração, encapsulamento, herança e polimorfismo no domínio de personagens, habilidades e partidas em Java.
- **Banco de Dados:** modelar dados relacionais e preparar exemplos de integridade, relacionamentos, CRUD futuro, trigger, índice, permissões e view de ranking com PostgreSQL.

## Tecnologias

- Frontend: HTML5, CSS3, JavaScript puro e Vite.
- Backend: Java 21, Spring Boot e API REST.
- Banco de dados: PostgreSQL; scripts relacionais em `database/` e leitura JDBC opcional pelo perfil `postgres`.
- Ferramentas: Maven Wrapper, npm e DBeaver para a futura execução dos scripts SQL.

## Funcionalidades atuais

- Menu principal e seleção de Guerreiro, Mago ou Arqueiro.
- Arena com tabuleiro 8×8, movimentação ortogonal, alcance e interface adaptável.
- Ataque básico, habilidade especial, mana, turnos e comportamento automático do Goblin.
- Resultado de vitória ou derrota e possibilidade de iniciar outra partida.
- API REST para consultar personagens e executar partidas. O frontend usa o backend como autoridade para as ações de combate.
- No perfil `postgres`, consulta de personagens e habilidades via Spring JDBC, além do endpoint `GET /api/status/database`.
- Sprites opcionais com placeholders quando os arquivos de imagem ainda não foram adicionados.

As partidas do backend são armazenadas somente em memória e se perdem ao reiniciar a aplicação.

## Conceitos de POO no backend

- `Personagem` é uma classe abstrata com atributos encapsulados e comportamentos comuns.
- `Guerreiro`, `Mago`, `Arqueiro` e `Goblin` herdam de `Personagem`.
- Cada classe implementa a execução da habilidade especial, demonstrando polimorfismo.
- `Habilidade` representa efeitos com dados próprios e validações.
- `Partida`, `Tabuleiro` e `Posicao` organizam o domínio do combate; serviços coordenam as regras e controllers expõem DTOs pela API.

## Estrutura do repositório

```text
ArenaRPG/
├── backend/       API Spring Boot, código Java, testes e Maven Wrapper
├── database/      scripts PostgreSQL, testes SQL preparados e instruções
├── docs/           documentação técnica, acadêmica e prompts das etapas
├── frontend/       aplicação Vite, telas, serviços, estilos e testes
├── .gitignore      exclusões comuns e proteção de configurações locais
└── README.md       visão geral e instruções deste projeto
```

## Como executar no Windows

Pré-requisitos: Windows 11, Java 21, Node.js compatível com Vite 8 (`^20.19.0` ou `>=22.12.0`) e npm. Não é necessário configurar PostgreSQL para executar o jogo atualmente.

### Frontend

No PowerShell, na raiz do projeto:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Abra `http://localhost:5173/`. O servidor de desenvolvimento Vite encaminha as chamadas `/api` para `http://localhost:8080`.

### Backend

Em outro terminal PowerShell:

```powershell
cd backend
$env:MAVEN_OPTS = "-Duser.home=$env:USERPROFILE -Dmaven.repo.local=$env:USERPROFILE\.m2\repository"
.\mvnw.cmd spring-boot:run
```

A API fica disponível em `http://localhost:8080`. Para executar os testes Java, use `.\mvnw.cmd test` dentro de `backend`.

### Testes locais do frontend

```powershell
cd frontend
npm.cmd test
npm.cmd run build
```

## Scripts PostgreSQL e requisitos acadêmicos

Os arquivos SQL usam recursos nativos do PostgreSQL e foram preparados para execução pelo editor SQL do DBeaver, na ordem abaixo:

1. `database/01_tabelas.sql` — schema, tabelas e integridades de entidade, domínio, referência e regras definidas pelo projeto.
2. `database/02_indices.sql` — índice do histórico de partidas por jogador.
3. `database/03_triggers.sql` — validação do ciclo de partida e contabilização de vitórias/derrotas.
4. `database/04_views.sql` — view `vw_ranking_jogadores`, sem dados de autenticação.
5. `database/05_permissoes.sql` — roles com privilégios distintos para aplicação e consulta.
6. `database/06_dados_iniciais.sql` — catálogo de personagens e habilidades.
7. `database/07_testes.sql` — verificações transacionais, encerradas com `ROLLBACK`.
8. `database/08_demonstracao.sql` — consultas somente de leitura e demonstração opcional revertida com `ROLLBACK`.

Os scripts 01–07 foram executados manualmente pelo usuário no banco local. O script 08 foi revisado estaticamente na Etapa 16B, mas ainda não foi executado. A conexão inicial está implementada no backend sob o perfil `postgres`, mas não foi validada contra o servidor PostgreSQL nesta etapa. Consulte [database/README.md](database/README.md) para detalhes do modelo, das permissões e da demonstração acadêmica. Não configure credenciais reais nos scripts ou na documentação.

## Estado do desenvolvimento

O frontend já consome a API Java para as batalhas. Com o perfil `postgres`, a API passa a ler o catálogo de personagens e habilidades no banco; sem esse perfil, permanece no comportamento em memória. A conexão e o catálogo JDBC ainda precisam ser validados no ambiente PostgreSQL local. Partidas continuam em memória. Cadastro/login, persistência de partidas e ranking real dependem de etapas futuras.

Para configurar e iniciar a API com o perfil PostgreSQL, siga [docs/integracao-postgresql.md](docs/integracao-postgresql.md). As variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` são fornecidas localmente, sem senhas versionadas.

Documentação acadêmica e rastreabilidade do uso de IA estão em `docs/`, incluindo os prompts registrados em `docs/prompts/`. Consulte também [docs/requisitos-banco-dados.md](docs/requisitos-banco-dados.md) e [docs/integracao-frontend-backend.md](docs/integracao-frontend-backend.md).

