package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaDescontoTest {

    private final PoliticaDesconto politica = new PoliticaDesconto();

    @Test
    void deveRejeitarSubtotalNegativo() {
        Cliente cliente = new Cliente(false, false, 0);
        assertThrows(IllegalArgumentException.class, () -> politica.calcular(cliente, -1, null));
    }

    @Test
    void deveAplicarDescontoBaseConformeTipoDeClienteESemCupom() {
        Cliente vip = new Cliente(true, false, 1);
        Cliente comum = new Cliente(false, false, 1);

        assertEquals(1_000L, politica.calcular(vip, 10_000, null));
        assertEquals(0L, politica.calcular(comum, 49_999, null));
        assertEquals(2_500L, politica.calcular(comum, 50_000, null));
        assertEquals(0L, politica.calcular(new Cliente(false, false, 0), 1_000, "   "));
    }

    @Test
    void devePermitirBemVindoEExtra10ConformeElegibilidade() {
        Cliente novo = new Cliente(false, false, 0);
        Cliente comHistorico = new Cliente(false, false, 1);

        assertEquals(2_000L, politica.calcular(novo, 10_000, "BEMVINDO"));
        assertEquals(0L, politica.calcular(comHistorico, 10_000, "BEMVINDO"));
        assertEquals(0L, politica.calcular(novo, 9_999, "BEMVINDO"));
        assertEquals(2_000L, politica.calcular(comHistorico, 20_000, "EXTRA10"));
        assertEquals(0L, politica.calcular(comHistorico, 19_999, "EXTRA10"));
        assertEquals(2_000L, politica.calcular(novo, 10_000, "  bemvindo  "));
    }

    @Test
    void deveRejeitarCupomDesconhecidoELimitarDescontoAoTeto() {
        Cliente comum = new Cliente(false, false, 1);
        assertThrows(IllegalArgumentException.class,
            () -> politica.calcular(comum, 10_000, "NAOEXISTE"));

        Cliente vip = new Cliente(true, false, 1);
        long subtotal = 100_000;
        assertEquals(subtotal * 20 / 100, politica.calcular(vip, subtotal, "EXTRA10"));

        Cliente novoVip = new Cliente(true, false, 0);
        long subtotalPequeno = 10_000;
        assertEquals(subtotalPequeno * 20 / 100, politica.calcular(novoVip, subtotalPequeno, "BEMVINDO"));
    }
}
