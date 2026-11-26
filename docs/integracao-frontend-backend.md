# Integração frontend e backend

## Execução local no Windows

Use dois terminais no VS Code.

Terminal 1, backend (JDK 21):

```powershell
cd C:\Users\Gui\Documents\ArenaRPG\backend
java -version
$env:MAVEN_OPTS = '-Dmaven.repo.local=C:/Users/Gui/.m2/repository'
.\mvnw.cmd spring-boot:run
```

Terminal 2, frontend:

```powershell
cd C:\Users\Gui\Documents\ArenaRPG\frontend
npm.cmd install
npm.cmd run dev
```

Abra `http://localhost:5173/`. O proxy de desenvolvimento do Vite encaminha `/api/*` para `http://localhost:8080`. O backend não abre CORS global. Para criar o pacote do frontend e executar os testes locais: `npm.cmd run build` e `npm.cmd test`. Para validar o backend no Terminal 1 (com MAVEN_OPTS configurado), execute: `cd C:\Users\Gui\Documents\ArenaRPG\backend` e `.\mvnw.cmd test`.

## Fonte do estado

O frontend consulta `GET /api/personagens` para desenhar as opções e `GET /api/personagens/{tipo}` ao escolher uma classe. Uma nova partida é criada em `POST /api/partidas`; o snapshot inicial é confirmado com `GET /api/partidas/{id}`. Os comandos de mover, ataque, habilidade e encerramento de turno usam os endpoints da partida e substituem o snapshot visual pela resposta Java. A tela de resultado também deriva do estado retornado pelo backend.

O frontend não aplica dano, consome mana, executa a IA ou determina vitória/derrota. Ele calcula somente a prévia visual das células de movimento e o realce de alcance com base nos atributos/posições recebidos. O Java valida as ações e é a autoridade final.

Durante uma requisição, ações da arena ficam bloqueadas e a interface mostra que está aguardando o backend. Erros HTTP são exibidos sem alteração otimista do estado. Em falha de conexão durante uma ação, o frontend consulta o estado atual antes de permitir novas ações; se não conseguir sincronizar, mantém as ações bloqueadas até reconectar ou voltar ao menu. Uma partida removida/reiniciada no servidor retorna o jogador à seleção com uma mensagem.

## Contratos

- Classes: `GET /api/personagens` e `GET /api/personagens/{tipo}`.
- Iniciar: `POST /api/partidas` com `{"tipoPersonagem":"mago"}`.
- Consultar: `GET /api/partidas/{id}`.
- Mover: `POST /api/partidas/{id}/mover` com `{"x":2,"y":6}`.
- Ataque: `POST /api/partidas/{id}/atacar`, sem corpo.
- Habilidade: `POST /api/partidas/{id}/habilidade`, sem corpo.
- Encerrar turno e executar Goblin: `POST /api/partidas/{id}/finalizar-turno`, sem corpo.

Os comandos válidos devolvem snapshots JSON completos de `PartidaDTO`. Erros seguem as respostas HTTP do backend (400, 404 ou 409) e aparecem na interface. O formato detalhado da API está em [API-PARTIDAS.md](../backend/API-PARTIDAS.md).

## Requisitos acadêmicos de Banco de Dados

Os cinco itens exigidos pelo professor e as propostas para a etapa PostgreSQL estão em [requisitos-banco-dados.md](requisitos-banco-dados.md). Nesta etapa não há conexão ou persistência; as partidas Java continuam em memória.
