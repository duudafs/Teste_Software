package br.edu.ifpr.boletim;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoletimTest {

    @Test
    void deveAprovarAlunoComMediaOito() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(8);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRecuperarNotaAlunoComMediaQuatro() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(4);

        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarAlunoComMediaDois() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(2);

        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveCalcularMediaIgualCinco() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(5,5);

        assertEquals(5,resultado);

    }

    @Test
    void deveCalcularMediaComParteDecimal() {
        Boletim boletim = new Boletim();

        double resultado = boletim.calcularMedia(7, 8);

        assertEquals(7.5, resultado, 0.0001);
    }

    @Test
    void deveAprovarNoLimiteExatoSete() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(7);

        assertEquals("APROVADO", resultado);
    }

    @Test
    void deveRecuperarLogoAbaixoDoLimiteSete() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(6.9);

        assertEquals("RECUPERACAO", resultado);
    }

    @Test
    void deveReprovarLogoAbaixoDoLimiteQuatro() {
        Boletim boletim = new Boletim();

        String resultado = boletim.verificarSituacao(3.9);

        assertEquals("REPROVADO", resultado);
    }

    @Test
    void deveContarZeroAprovadosComArrayVazio() {
        Boletim boletim = new Boletim();

        int quantidade = boletim.contarAprovados(new double[] {});

        assertEquals(0, quantidade);
    }

    @Test
    void deveContarUmAprovadoComArrayDeUmElemento() {
        Boletim boletim = new Boletim();

        int quantidade = boletim.contarAprovados(new double[] {8});

        assertEquals(1, quantidade);
    }

    @Test
    void deveContarAprovadosEntreVariosElementos() {
        Boletim boletim = new Boletim();

        int quantidade = boletim.contarAprovados(new double[] {8, 5, 7, 3, 9});

        assertEquals(3, quantidade);
    }
}
