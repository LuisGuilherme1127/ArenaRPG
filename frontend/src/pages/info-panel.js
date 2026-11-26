import { icon } from '../components/icons.js'

const panels = {
  personagens: {
    eyebrow: 'SEU LEGADO', title: 'Personagens',
    message: 'Sua coleção de heróis e suas jornadas aparecerão aqui. A aventura ainda está sendo preparada.',
    icon: 'people',
  },
  ranking: {
    eyebrow: 'OS MAIS VALENTES', title: 'Ranking',
    message: 'O placar dos grandes aventureiros será revelado quando a arena abrir suas portas.',
    icon: 'ranking',
  },
  configuracoes: {
    eyebrow: 'AJUSTE SUA JORNADA', title: 'Configurações',
    message: 'As opções de áudio, acessibilidade e preferências estarão disponíveis em uma próxima etapa.',
    icon: 'settings',
  },
}

export function renderInfoPanel(panelId) {
  const panel = panels[panelId] ?? panels.personagens
  return `
    <main class="screen info-screen">
      <div class="ambient ambient-one" aria-hidden="true"></div>
      <header class="topbar">
        <button class="brand-mark brand-button" type="button" data-action="home" aria-label="Voltar ao início">
          <span class="brand-shield">A</span><span class="brand-name">ARENA <b>RPG</b></span>
        </button>
        <span class="topbar-note">CRÔNICAS DE ELDORIA <i></i> CAPÍTULO I</span>
      </header>
      <section class="info-content" aria-labelledby="page-title">
        <div class="info-panel">
          <div class="info-emblem">${icon(panel.icon)}</div>
          <p class="eyebrow"><span></span> ${panel.eyebrow} <span></span></p>
          <h1 id="page-title" tabindex="-1">${panel.title}</h1>
          <p class="info-message">${panel.message}</p>
          <span class="coming-soon"><i></i> DISPONÍVEL EM BREVE</span>
          <button class="select-button info-back" type="button" data-action="home">${icon('back')} <span>Voltar ao menu</span></button>
        </div>
      </section>
    </main>
  `
}
