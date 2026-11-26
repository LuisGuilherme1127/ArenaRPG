const CLASS_PRESENTATION = {
  guerreiro: {
    title: 'AÇO E CORAGEM',
    description: 'Resiste aos golpes mais pesados e enfrenta qualquer perigo de frente.',
    symbol: 'G',
  },
  mago: {
    title: 'SABEDORIA ARCANA',
    description: 'Canaliza forças antigas para lançar feitiços de poder extraordinário.',
    symbol: 'M',
  },
  arqueiro: {
    title: 'PRECISÃO E AGILIDADE',
    description: 'Ataca de longe com precisão e se move antes que o perigo se aproxime.',
    symbol: 'A',
  },
}

export function mapApiCharacter(character) {
  const presentation = CLASS_PRESENTATION[character.tipo]
  const ability = character.habilidades?.[0]
  if (!presentation || !ability) {
    throw new Error(`A API retornou dados incompletos para ${character.nome ?? 'a classe selecionada'}.`)
  }

  return {
    id: character.tipo,
    name: character.nome,
    title: presentation.title,
    description: presentation.description,
    symbol: presentation.symbol,
    maxHealth: character.vidaMaxima,
    maxMana: character.manaMaxima,
    attack: character.ataque,
    range: character.alcance,
    movementMax: character.movimentoMaximo,
    abilityName: ability.nome,
    abilityDamage: ability.dano,
    abilityCost: ability.custoMana,
    abilityRange: ability.alcance,
  }
}

export function buildCharacterCards(apiCharacters) {
  const characters = apiCharacters
    .filter((character) => CLASS_PRESENTATION[character.tipo])
    .map(mapApiCharacter)

  const expectedTypes = Object.keys(CLASS_PRESENTATION)
  const actualTypes = new Set(characters.map((character) => character.id))
  if (characters.length !== expectedTypes.length || expectedTypes.some((type) => !actualTypes.has(type))) {
    throw new Error('A API não retornou todas as classes jogáveis esperadas.')
  }
  return characters.sort((first, second) => expectedTypes.indexOf(first.id) - expectedTypes.indexOf(second.id))
}

export function getClassPresentation(type) {
  return CLASS_PRESENTATION[type] ?? { symbol: '?' }
}
