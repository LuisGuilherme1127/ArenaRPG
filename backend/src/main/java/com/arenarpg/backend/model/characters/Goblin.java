package com.arenarpg.backend.model.characters;

import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.ResultadoAcao;
import com.arenarpg.backend.model.TipoPersonagem;

public class Goblin extends Personagem {
    public Goblin() {
        super(TipoPersonagem.GOBLIN, "Goblin", 100, 12, 0, 0, 1, 2, null);
    }

    @Override
    public ResultadoAcao executarHabilidadeEspecial(Personagem alvo, int distanciaManhattan) {
        return ResultadoAcao.falha("Goblin não possui habilidade especial.");
    }
}
