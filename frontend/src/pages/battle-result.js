import { icon } from '../components/icons.js'

export function renderBattleResult(state) {
  const victory = state.status === 'victory'
  return `
    <main class="screen result-screen ${victory ? 'result-victory' : 'result-defeat'}">
      <div class="ambient ambient-one" aria-hidden="true"></div>
      <header class="topbar">
        <span class="brand-mark"><span class="brand-shield">A</span><span class="brand-name">ARENA <b>RPG</b></span></span>
        <span class="topbar-note">CRÔNICAS DE ELDORIA <i></i> CAPÍTULO I</span>
      </header>
      <section class="result-content" aria-labelledby="result-title">
        <div class="result-emblem">${icon(victory ? 'shield' : 'sword')}</div>
        <p class="eyebrow"><span></span> ${victory ? 'A ARENA RECONHECE SUA FORÇA' : 'TODA LENDA TEM SEUS DESAFIOS'} <span></span></p>
        <h1 id="result-title" tabindex="-1">${victory ? 'Vitória' : 'Derrota'}</h1>
        <p class="result-copy">${victory ? `${state.player.name}, o Goblin foi vencido. Sua jornada continua.` : `${state.player.name}, você foi derrotado pelo Goblin. Reúna forças e tente novamente.`}</p>
        <div class="result-summary"><span>GOBLIN <b>${state.enemy.health} / ${state.enemy.maxHealth} PV</b></span><i></i><span>${state.round} ${state.round === 1 ? 'TURNO' : 'TURNOS'}</span></div>
        <div class="result-actions">
          <button class="result-button result-primary" type="button" data-action="play-again">Jogar novamente ${icon('arrow')}</button>
          <button class="result-button" type="button" data-action="home">Voltar ao menu</button>
        </div>
      </section>
    </main>
  `
}
