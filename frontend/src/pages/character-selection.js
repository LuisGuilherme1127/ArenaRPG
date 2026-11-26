import { icon } from '../components/icons.js'
import { escapeHtml } from '../services/escape-html.js'
import { renderSprite } from '../services/sprite-assets.js'

function stat(label, value, maximum) {
  const percent = Math.round((value / maximum) * 100)
  return `<div class="stat-row"><span>${label}</span><span class="stat-track"><i style="--stat:${percent}%"></i></span><b>${value}</b></div>`
}

export function renderCharacterSelection(
  selectedClass,
  sprites = {},
  characters = [],
  isLoading = false,
  isPending = false,
  notice = '',
) {
  return `
    <main class="screen selection-screen">
      <div class="ambient ambient-one" aria-hidden="true"></div>
      <header class="topbar">
        <button class="brand-mark brand-button" type="button" data-action="home" aria-label="Voltar ao início">
          <span class="brand-shield">A</span><span class="brand-name">ARENA <b>RPG</b></span>
        </button>
        <span class="topbar-note">CRÔNICAS DE ELDORIA <i></i> CAPÍTULO I</span>
      </header>

      <section class="selection-content" aria-labelledby="page-title">
        <div class="selection-heading">
          <p class="eyebrow"><span></span> SUA JORNADA COMEÇA COM UMA ESCOLHA <span></span></p>
          <h1 id="page-title" tabindex="-1">Escolha seu <em>herói</em></h1>
          <p>Três caminhos se abrem diante de você. Qual deles seguirá sua lenda?</p>
        </div>

        ${notice ? `<p class="api-notice" role="alert">${escapeHtml(notice)}</p>` : ''}
        <div class="character-grid">
          ${isLoading
    ? '<p class="selection-loading" role="status">Consultando as classes no backend...</p>'
    : characters.map((character, index) => {
            const isSelected = selectedClass === character.id
            const sprite = renderSprite(character.id, sprites)
            return `
              <article class="character-card ${isSelected ? 'is-selected' : ''}" style="--card-order:${index}">
                <div class="character-art art-${character.id} sprite-frame ${sprite ? 'has-sprite' : ''}" aria-hidden="true">
                  <div class="art-halo"></div><div class="art-sigil">${character.symbol}</div>
                  ${sprite}
                  <span class="art-line art-line-one"></span><span class="art-line art-line-two"></span>
                  <span class="art-caption">ILUSTRAÇÃO EM BREVE</span>
                  <span class="art-number">0${index + 1}</span>
                </div>
                <div class="character-info">
                  <p class="character-title">${character.title}</p>
                  <h2>${character.name}</h2>
                  <p class="character-description">${character.description}</p>
                  <div class="character-stats" aria-label="Atributos iniciais demonstrativos">
                    ${stat('Vida', character.maxHealth, 120)}${stat('Mana', character.maxMana, 100)}
                    ${stat('Ataque', character.attack, 35)}${stat('Alcance', character.range, 4)}
                  </div>
                  <p class="character-ability"><b>${character.abilityName}</b> · ${character.abilityDamage} de dano · ${character.abilityCost} de mana</p>
                  <div class="class-actions">
                    <button class="select-button ${isSelected ? 'selected-button' : ''}" type="button"
                      data-action="select" data-character="${character.id}" aria-pressed="${isSelected}" ${isPending ? 'disabled' : ''}>
                      ${isPending ? '<span>Consultando classe...</span>' : isSelected ? `${icon('shield')} <span>Personagem escolhido</span>` : `<span>Escolher classe</span> ${icon('arrow')}`}
                    </button>
                    ${isSelected ? `<button class="select-button battle-start-button" type="button" data-action="start-battle" ${isPending ? 'disabled' : ''}>${isPending ? 'Iniciando batalha...' : 'Começar batalha'}</button>` : ''}
                  </div>
                </div>
                ${isSelected ? '<span class="selected-ribbon">ESCOLHIDO</span>' : ''}
              </article>
            `
          }).join('')}
          ${!isLoading && characters.length === 0 ? '<button class="select-button" type="button" data-action="retry-characters">Tentar novamente</button>' : ''}
        </div>

        <div class="selection-bottom">
          <button class="back-button" type="button" data-action="home">${icon('back')} <span>Voltar ao menu</span></button>
          <p class="selection-feedback" aria-live="polite">${selectedClass ? `${characters.find((character) => character.id === selectedClass)?.name ?? 'Personagem'} escolhido. Sua aventura está prestes a começar.` : 'Você poderá mudar sua escolha antes de iniciar a aventura.'}</p>
          <span class="selection-step">PASSO <b>${selectedClass ? '02' : '01'}</b> <i>/</i> 03</span>
        </div>
      </section>
    </main>
  `
}
