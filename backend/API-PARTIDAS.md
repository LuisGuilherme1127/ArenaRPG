# API de partidas

As partidas ficam em memória e recebem um UUID novo. Personagens e tabuleiro são retornados como snapshots JSON; nenhuma entidade mutável é exposta. Movimentos usam coordenadas `x` e `y` de 0 a 7.

## Iniciar — `POST /api/partidas`

Requisição:

```json
{"tipoPersonagem":"guerreiro"}
```

Aceita `guerreiro`, `mago` ou `arqueiro` (sem diferenciar maiúsculas). Retorna HTTP 201 e o estado inicial:

```json
{
  "id":"b4f640fc-8122-4dc7-856c-9473bfa273c7",
  "estado":"em_andamento",
  "turnoAtual":"jogador",
  "rodada":1,
  "jogador":{"tipo":"guerreiro","nome":"Guerreiro","vidaMaxima":120,"vidaAtual":120,"ataque":20,"defesa":0,"manaMaxima":30,"manaAtual":30,"alcance":1,"movimentoMaximo":3,"habilidades":[{"nome":"Golpe Poderoso","descricao":"Um golpe corpo a corpo de grande força.","dano":35,"custoMana":10,"alcance":1,"tipo":"fisica"}]},
  "inimigo":{"tipo":"goblin","nome":"Goblin","vidaMaxima":100,"vidaAtual":100,"ataque":12,"defesa":0,"manaMaxima":0,"manaAtual":0,"alcance":1,"movimentoMaximo":2,"habilidades":[]},
  "tabuleiro":{"linhas":8,"colunas":8,"posicaoJogador":{"x":1,"y":6},"posicaoGoblin":{"x":6,"y":2}},
  "movimentoRealizado":false,
  "acaoOfensivaRealizada":false,
  "eventos":[{"rodada":1,"tipo":"sistema","mensagem":"A batalha começou. Guerreiro enfrenta o Goblin."}]
}
```

## Consultar — `GET /api/partidas/{id}`

Não recebe corpo. Retorna HTTP 200 com o snapshot da partida no mesmo formato da criação.

## Mover — `POST /api/partidas/{id}/mover`

```json
{"x":2,"y":6}
```

Retorna o snapshot atualizado (HTTP 200). A posição deve estar no tabuleiro, livre e acessível em até três células ortogonais para o jogador.

## Ataque básico — `POST /api/partidas/{id}/atacar`

Sem corpo. Retorna o snapshot atualizado (HTTP 200). O alvo único é o Goblin.

## Habilidade — `POST /api/partidas/{id}/habilidade`

Sem corpo. Retorna o snapshot atualizado (HTTP 200). Usa a habilidade especial da classe escolhida.

## Finalizar turno — `POST /api/partidas/{id}/finalizar-turno`

Sem corpo. O backend executa o turno do Goblin antes de devolver o snapshot (HTTP 200).

## Erros

Os erros seguem `ProblemDetail` do Spring. JSON inválido, tipo de personagem desconhecido ou coordenada inválida retorna HTTP 400; UUID sem partida retorna 404; ação incompatível com turno, alcance, mana ou estado retorna 409. Exemplo:

```json
{"type":"about:blank","title":"Ação incompatível com a partida","status":409,"detail":"O alvo está fora do alcance do ataque básico."}
```
