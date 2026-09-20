package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    @Test
    void deveFecharPedidoDeClienteComumComFreteDoParanaEPagamentoAprovado() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedido, cliente);

        assertAll(
            () -> assertEquals("PAGO", resultado.status()),
            () -> assertEquals(10_000L, resultado.subtotalCentavos()),
            () -> assertEquals(0L, resultado.descontoCentavos()),
            () -> assertEquals(1_200L, resultado.freteCentavos()),
            () -> assertEquals(11_200L, resultado.totalCentavos()),
            () -> assertEquals(List.of(11_200L), cobrancas)
        );
    }

    @Test
    void bloqueadoESubtotalZeroInterrompemAntesDeQualquerCobranca() {
        Cliente bloqueado = new Cliente(false, true, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedidoValido = new Pedido(List.of(item), "PR", false, null);
        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        ResultadoPedido resultado = service.fechar(pedidoValido, bloqueado);
        assertEquals("BLOQUEADO", resultado.status());
        assertEquals(0L, resultado.totalCentavos());

        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido inativo = new ItemPedido("LIVRO-JAVA", 10_000, 0, 5, 1_000, false);
        Pedido pedidoSemItensAtivos = new Pedido(List.of(inativo), "PR", false, null);
        assertThrows(IllegalArgumentException.class, () -> service.fechar(pedidoSemItensAtivos, cliente));

        assertTrue(cobrancas.isEmpty());
    }

    @Test
    void semEstoqueERiscoPendenteRetornamSemCobrar() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido semEstoque = new ItemPedido("LIVRO-JAVA", 10_000, 5, 1, 1_000, false);
        Pedido pedidoSemEstoque = new Pedido(List.of(semEstoque), "PR", false, null);

        List<Long> cobrancas = new ArrayList<>();
        PedidoService service = new PedidoService(total -> {
            cobrancas.add(total);
            return true;
        });

        assertEquals("SEM_ESTOQUE", service.fechar(pedidoSemEstoque, cliente).status());

        Cliente novo = new Cliente(false, false, 0);
        ItemPedido caro = new ItemPedido("NOTEBOOK", 200_000, 1, 5, 1_000, false);
        Pedido pedidoDeRisco = new Pedido(List.of(caro), "PR", false, null);

        ResultadoPedido resultado = service.fechar(pedidoDeRisco, novo);
        assertEquals("REVISAO", resultado.status());
        assertEquals(200_000L, resultado.subtotalCentavos());
        assertTrue(cobrancas.isEmpty());
    }

    @Test
    void deveRetornarPagamentoRecusadoENuloRejeitaReferenciasObrigatorias() {
        Cliente cliente = new Cliente(false, false, 1);
        ItemPedido item = new ItemPedido("LIVRO-JAVA", 10_000, 1, 5, 1_000, false);
        Pedido pedido = new Pedido(List.of(item), "PR", false, null);
        PedidoService recusa = new PedidoService(total -> false);
        PedidoService aprova = new PedidoService(total -> true);

        assertEquals("PAGAMENTO_RECUSADO", recusa.fechar(pedido, cliente).status());
        assertThrows(NullPointerException.class, () -> aprova.fechar(null, cliente));
        assertThrows(NullPointerException.class, () -> aprova.fechar(pedido, null));
    }
}
