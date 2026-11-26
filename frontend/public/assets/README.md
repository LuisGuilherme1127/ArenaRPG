# Assets visuais do Arena RPG

Adicione os sprites de personagem e inimigo nestes caminhos:

- `characters/guerreiro.png`
- `characters/mago.png`
- `characters/arqueiro.png`
- `enemies/goblin.png`

O frontend verifica a disponibilidade dos arquivos antes de renderizá-los. Sem um PNG válido, os símbolos e a arte em CSS continuam visíveis.

Os sprites de personagem funcionam melhor como PNGs transparentes quadrados de **512 × 512 px**, com o corpo inteiro centralizado e margem transparente ao redor. Para jogos em pixel art, **128 × 128 px** também é adequado; nesse caso, marque o recurso como pixel art em `src/services/sprite-assets.js`.

Os espaços para outros recursos ficam em `backgrounds/`, `textures/`, `effects/` e `icons/`. O tabuleiro continuará desenhando cada célula separadamente para preservar destaques e interação.
