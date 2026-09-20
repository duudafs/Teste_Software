package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnaliseRiscoTest {

    private final AnaliseRisco risco = new AnaliseRisco();

    @Test
    void deveRejeitarTotalNegativoERecusarBloqueado() {
        Cliente cliente = new Cliente(false, false, 0);
        assertThrows(IllegalArgumentException.class, () -> risco.avaliar(cliente, -1, false));

        assertEquals("RECUSADO", risco.avaliar(new Cliente(true, true, 10), 1, false));
        assertEquals("RECUSADO", risco.avaliar(new Cliente(false, true, 0), 1_000_000, true));
    }

    @Test
    void clienteNovoVaiParaRevisaoPorTotalOuPorExpresso() {
        Cliente novo = new Cliente(false, false, 0);

        assertEquals("REVISAO", risco.avaliar(novo, 100_001, false));
        assertEquals("REVISAO", risco.avaliar(novo, 1, true));
        assertEquals("APROVADO", risco.avaliar(novo, 100_000, false));
    }

    @Test
    void clienteComHistoricoConsideraVipParaRevisao() {
        Cliente comHistorico = new Cliente(false, false, 3);
        Cliente vip = new Cliente(true, false, 3);

        assertEquals("REVISAO", risco.avaliar(comHistorico, 500_001, false));
        assertEquals("APROVADO", risco.avaliar(vip, 500_001, false));
        assertEquals("APROVADO", risco.avaliar(comHistorico, 500_000, false));
    }
}
