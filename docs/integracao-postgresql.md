# Integração inicial com PostgreSQL — Etapa 16A

## Estado e limites

O backend usa Spring JDBC e `JdbcTemplate` para consultar o catálogo de personagens e habilidades quando o perfil `postgres` está ativo. O perfil padrão continua usando os objetos Java em memória. O banco não é necessário para iniciar a aplicação no modo padrão.

Esta etapa não executa nem cria tabelas. A inicialização automática de SQL do Spring está desativada. As partidas, regras de combate e personagens usados pelo motor continuam no Java e em memória; esta integração não implementa login, persistência de partidas ou operações de escrita no PostgreSQL.

## Arquitetura

- `PersonagemController` mantém os endpoints públicos e o contrato JSON atual.
- `PersonagemCatalogo` separa a origem dos dados consultados pelo controller.
- `MemoriaPersonagemCatalogo`, ativo sem o perfil `postgres`, delega ao serviço Java existente.
- `JdbcPersonagemRepository`, ativo no perfil `postgres`, usa `JdbcTemplate` e faz `LEFT JOIN` entre `arena.personagens` e `arena.habilidades`.
- `PersonagemService`, `Personagem` e suas subclasses seguem como fonte das regras do motor de combate.
- `PostgresDatabaseStatusService` verifica a conexão com `SELECT 1`; o serviço inativo informa explicitamente que o perfil não está habilitado.
- O `DataSource` Hikari é criado somente no perfil PostgreSQL. A configuração da aplicação exclui a criação automática de `DataSource` para que o perfil padrão não dependa de banco.

## Compatibilidade entre tabelas e domínio Java

`arena.personagens` fornece tipo, nome e atributos máximos. `arena.habilidades` fornece descrição, dano, custo, alcance e tipo; a coluna `personagem_id` relaciona cada habilidade à classe. Vida e mana atuais não existem nas tabelas e são mapeadas para os máximos porque as instâncias novas do domínio começam com os recursos cheios.

O repositório converte os tipos de habilidade para minúsculas, preservando o JSON atual. Ele também compara o catálogo retornado pelo banco aos DTOs gerados pelas classes Java. Se algum atributo, habilidade, tipo, ou personagem estiver faltando/divergente, o endpoint retorna `503` com uma mensagem de revisão, sem substituir os valores do combate.

Na inspeção, os dados de seed dos quatro personagens e três habilidades correspondem aos construtores Java: Guerreiro (120 vida, 20 ataque, 0 defesa, 30 mana, alcance 1, movimento 3); Mago (80, 15, 0, 100, 3, 3); Arqueiro (90, 18, 0, 50, 4, 3); Goblin (100, 12, 0, 0, 1, 2). As habilidades também coincidem em dano, custo, alcance e tipo.

## Dependências

O `pom.xml` inclui:

- `spring-boot-starter-jdbc` para JDBC e `JdbcTemplate`.
- `org.postgresql:postgresql` em escopo runtime para o driver PostgreSQL.

Java permanece na versão 21 e o Maven Wrapper existente é mantido. Não foram adicionados JPA, Hibernate ou Flyway.

## Configuração local no PowerShell

O perfil `postgres` lê `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` do ambiente. Configure o usuário `arena_app` no banco por meio do administrador; scripts ou arquivos versionados não definem senhas.

No PowerShell, entre em `backend` e defina as variáveis apenas para a sessão atual. O exemplo usa um prompt protegido para a senha, sem colocá-la na linha de comando ou no histórico:

```powershell
$env:MAVEN_OPTS = "-Duser.home=$env:USERPROFILE -Dmaven.repo.local=$env:USERPROFILE\.m2\repository"
$env:SPRING_PROFILES_ACTIVE = 'postgres'
$env:DB_URL = 'jdbc:postgresql://localhost:5432/arena_rpg?currentSchema=arena'
$env:DB_USERNAME = 'arena_app'
$securePassword = Read-Host 'Senha local de arena_app' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    $securePassword.Dispose()
}
.\mvnw.cmd spring-boot:run
```

Para executar sem PostgreSQL, não defina `SPRING_PROFILES_ACTIVE=postgres` (ou remova-a com `Remove-Item Env:SPRING_PROFILES_ACTIVE`); a aplicação usa o perfil padrão. As variáveis de conexão podem ser removidas ao terminar a sessão:

```powershell
Remove-Item Env:DB_URL, Env:DB_USERNAME, Env:DB_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue
```

## Endpoints

- `GET /api/status` — endpoint de status existente, inalterado.
- `GET /api/status/database` — verifica o banco com `SELECT 1` no perfil PostgreSQL. Responde `200` se disponível e `503` se indisponível. Sem o perfil, responde `503` com `status: desabilitado`, `integracaoHabilitada: false` e `disponivel: false`.
- `GET /api/personagens` e `GET /api/personagens/{tipo}` — mantêm o contrato JSON existente. Usam o catálogo em memória no perfil padrão e `SELECT ... LEFT JOIN ...` no perfil PostgreSQL.

Exemplo de status sem perfil PostgreSQL:

```json
{
  "status": "desabilitado",
  "integracaoHabilitada": false,
  "disponivel": false,
  "mensagem": "O perfil postgres está desativado; a integração com o banco não está habilitada."
}
```

Falhas de conexão e de consulta não incluem URL, usuário, senha ou detalhes do driver nas respostas HTTP.

## Consulta SQL da camada JDBC

A consulta lista o catálogo com as colunas usadas pelo contrato JSON e relaciona habilidades sem remover personagens que não tenham uma habilidade:

```sql
SELECT p.tipo, p.nome, p.vida_maxima, p.ataque, p.defesa, p.mana_maxima,
       p.alcance, p.movimento_maximo,
       h.nome AS habilidade_nome, h.descricao AS habilidade_descricao,
       h.dano AS habilidade_dano, h.custo_mana AS habilidade_custo_mana,
       h.alcance AS habilidade_alcance, h.tipo AS habilidade_tipo
FROM arena.personagens p
LEFT JOIN arena.habilidades h ON h.personagem_id = p.id
ORDER BY CASE p.tipo
    WHEN 'guerreiro' THEN 1 WHEN 'mago' THEN 2
    WHEN 'arqueiro' THEN 3 WHEN 'goblin' THEN 4 ELSE 5
END, h.id;
```

Para procurar por tipo, o repositório acrescenta `WHERE p.tipo = ?` e fornece o valor como parâmetro de `JdbcTemplate`; não concatena texto recebido pela API ao SQL.

## Testes

Executar os testes automatizados sem iniciar perfil PostgreSQL:

```powershell
cd backend
$env:MAVEN_OPTS = "-Duser.home=$env:USERPROFILE -Dmaven.repo.local=$env:USERPROFILE\.m2\repository"
.\mvnw.cmd test
```

Testes unitários adicionados cobrem a consulta parametrizada e o mapeamento do `JOIN`, a validação de consistência dos atributos, `SELECT 1`, respostas de banco disponível/indisponível e o endpoint no perfil padrão. O teste de contexto ativa o perfil PostgreSQL com valores fictícios e confirma que os beans JDBC inicializam sem executar uma consulta. Nenhum teste conecta a um PostgreSQL real; testes de integração com o servidor dependem da configuração local manual descrita acima.

O ambiente de desenvolvimento desta etapa resolveu `user.home` como `C:\`, o que impedia o Maven Wrapper de abrir o repositório local. A linha `MAVEN_OPTS` dos exemplos direciona o Maven ao cache do usuário Windows. Se o Maven do seu ambiente já estiver configurado corretamente, essa linha é opcional.

## Limitações e próxima etapa

- O catálogo é somente leitura; não há CRUD nem escrita no banco.
- As partidas permanecem em memória e o ranking SQL não está conectado à API.
- Não há autenticação, cadastro de usuário ou Row Level Security.
- A consistência entre banco e motor é verificada quando o catálogo é consultado; o catálogo divergente causa `503` para evitar exibir dados diferentes das regras do Java.
- Nenhum script da pasta `database` foi executado automaticamente ou por esta integração.
- Não foi realizado teste de integração contra PostgreSQL nesta etapa.

