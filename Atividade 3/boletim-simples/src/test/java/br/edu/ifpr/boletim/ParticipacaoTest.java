package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipacaoTest {

    @Test
    void deveDarZeroPontosQuandoNaoEntregouNemParticipou() {
        Participacao participacao = new Participacao();

        int pontos = participacao.calcularPontos(false, false);

        assertEquals(0, pontos);
    }

    @Test
    void deveDarDoisPontosQuandoSoEntregouAtividade() {
        Participacao participacao = new Participacao();

        int pontos = participacao.calcularPontos(true, false);

        assertEquals(2, pontos);
    }

    @Test
    void deveDarUmPontoQuandoSoParticipouDaAula() {
        Participacao participacao = new Participacao();

        int pontos = participacao.calcularPontos(false, true);

        assertEquals(1, pontos);
    }

    @Test
    void deveDarTresPontosQuandoEntregouEParticipou() {
        Participacao participacao = new Participacao();

        int pontos = participacao.calcularPontos(true, true);

        assertEquals(3, pontos);
    }
}
