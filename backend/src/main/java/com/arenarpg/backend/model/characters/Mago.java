package com.arenarpg.backend.model.characters;

import com.arenarpg.backend.model.Habilidade;
import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.ResultadoAcao;
import com.arenarpg.backend.model.TipoHabilidade;
import com.arenarpg.backend.model.TipoPersonagem;

public class Mago extends Personagem {
    public Mago() {
        super(
                TipoPersonagem.MAGO,
                "Mago",
                80,
                15,
                0,
                100,
                3,
                3,
                new Habilidade("Bola de Fogo", "Uma esfera de fogo que atinge um alvo à distância.", 30, 20, 3, TipoHabilidade.MAGICA));
    }

    @Override
    public ResultadoAcao executarHabilidadeEspecial(Personagem alvo, int distanciaManhattan) {
        return aplicarHabilidadeEspecial(alvo, distanciaManhattan);
    }
}
