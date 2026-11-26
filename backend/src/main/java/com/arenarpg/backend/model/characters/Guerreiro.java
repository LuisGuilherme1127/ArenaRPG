package com.arenarpg.backend.model.characters;

import com.arenarpg.backend.model.Habilidade;
import com.arenarpg.backend.model.Personagem;
import com.arenarpg.backend.model.ResultadoAcao;
import com.arenarpg.backend.model.TipoHabilidade;
import com.arenarpg.backend.model.TipoPersonagem;

public class Guerreiro extends Personagem {
    public Guerreiro() {
        super(
                TipoPersonagem.GUERREIRO,
                "Guerreiro",
                120,
                20,
                0,
                30,
                1,
                3,
                new Habilidade("Golpe Poderoso", "Um golpe corpo a corpo de grande força.", 35, 10, 1, TipoHabilidade.FISICA));
    }

    @Override
    public ResultadoAcao executarHabilidadeEspecial(Personagem alvo, int distanciaManhattan) {
        return aplicarHabilidadeEspecial(alvo, distanciaManhattan);
    }
}
