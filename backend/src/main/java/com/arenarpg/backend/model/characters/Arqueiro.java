package com.arenarpg.backend.model.characters;

import com.arenarpg.backend.model.Habilidade;
import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.ResultadoAcao;
import com.arenarpg.backend.model.TipoHabilidade;
import com.arenarpg.backend.model.TipoPersonagem;

public class Arqueiro extends Personagem {
    public Arqueiro() {
        super(
                TipoPersonagem.ARQUEIRO,
                "Arqueiro",
                90,
                18,
                0,
                50,
                4,
                3,
                new Habilidade("Flecha Precisa", "Um disparo preciso que alcança alvos distantes.", 28, 15, 4, TipoHabilidade.DISTANCIA));
    }

    @Override
    public ResultadoAcao executarHabilidadeEspecial(Personagem alvo, int distanciaManhattan) {
        return aplicarHabilidadeEspecial(alvo, distanciaManhattan);
    }
}
