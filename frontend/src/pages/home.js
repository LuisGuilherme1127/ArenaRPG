import { icon } from '../components/icons.js'

const menuItems = [
  { action: 'start', label: 'Iniciar aventura', icon: 'sword', featured: true },
  { action: 'panel', panel: 'personagens', label: 'Personagens', icon: 'people' },
  { action: 'panel', panel: 'ranking', label: 'Ranking', icon: 'ranking' },
  { action: 'panel', panel: 'configuracoes', label: 'Configurações', icon: 'settings' },
]

export function renderHome() {
  return `
    <main class="screen home-screen">
      <div class="ambient ambient-one" aria-hidden="true"></div>
      <div class="ambient ambient-two" aria-hidden="true"></div>
      <header class="topbar">
        <button class="brand-mark brand-button" type="button" data-action="home" aria-label="Arena RPG — início">
          <span class="brand-shield">A</span>
          <span class="brand-name">ARENA <b>RPG</b></span>
        </button>
        <span class="topbar-note">CRÔNICAS DE ELDORIA <i></i> CAPÍTULO I</span>
      </header>

      <section class="home-content" aria-labelledby="page-title">
        <div class="title-block">
          <p class="eyebrow"><span></span> UM MUNDO À SUA ESPERA <span></span></p>
          <h1 id="page-title" tabindex="-1">ARENA <em>RPG</em></h1>
          <p class="tagline">Sua lenda começa aqui.</p>
        </div>

        <div class="menu-frame">
          <div class="frame-corner corner-tl"></div><div class="frame-corner corner-tr"></div>
          <div class="frame-corner corner-bl"></div><div class="frame-corner corner-br"></div>
          <div class="frame-topline"><span></span> PORTAL DA AVENTURA <span></span></div>
          <nav class="main-menu" aria-label="Menu principal">
            ${menuItems.map((item, index) => `
              <button class="menu-button ${item.featured ? 'menu-button-primary' : ''}" type="button"
                data-action="${item.action}" ${item.panel ? `data-panel="${item.panel}"` : ''}>
                <span class="menu-icon">${icon(item.icon)}</span>
                <span class="menu-label">${item.label}</span>
                <span class="menu-arrow">${item.featured ? icon('arrow') : `<span>0${index + 1}</span>`}</span>
              </button>
            `).join('')}
          </nav>
          <p class="menu-hint">ESCOLHA SEU PRÓXIMO DESTINO</p>
        </div>
      </section>

      <footer class="home-footer">
        <span>FEITO PARA QUEM NASCEU PARA AVENTURAS</span>
        <span class="footer-divider"></span>
        <span>VERSÃO 0.1 <i class="status-dot"></i> PRÓLOGO</span>
      </footer>
    </main>
  `
}
