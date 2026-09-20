package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemPedidoTest {

    @Test
    void deveCriarItemValidoEExporCampos() {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 2, 10, 500, false);

        assertAll(
            () -> assertEquals("SKU-1", item.sku()),
            () -> assertEquals(1_000L, item.precoCentavos()),
            () -> assertEquals(2, item.quantidade()),
            () -> assertEquals(10, item.estoque()),
            () -> assertEquals(500, item.pesoGramas()),
            () -> assertFalse(item.fragil())
        );
    }

    @Test
    void deveRejeitarSkuPrecoEEstoqueInvalidos() {
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido(null, 1_000, 1, 1, 100, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("   ", 1_000, 1, 1, 100, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 0, 1, 1, 100, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000_001, 1, 1, 100, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, -1, 100, false));
    }

    @Test
    void deveValidarLimitesDeQuantidadeEPeso() {
        assertEquals(0, new ItemPedido("SKU-1", 1_000, 0, 1, 100, false).quantidade());
        assertEquals(100, new ItemPedido("SKU-1", 1_000, 100, 100, 100, false).quantidade());
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, -1, 1, 100, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 101, 200, 100, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, 1, 0, false));
        assertThrows(IllegalArgumentException.class,
            () -> new ItemPedido("SKU-1", 1_000, 1, 1, 100_001, false));
    }

    @Test
    void deveCalcularTotalEDisponibilidade() {
        ItemPedido item = new ItemPedido("SKU-1", 2_500, 3, 10, 100, false);
        assertEquals(7_500L, item.totalCentavos());

        assertTrue(new ItemPedido("SKU-1", 1_000, 5, 5, 100, false).disponivel());
        assertTrue(new ItemPedido("SKU-1", 1_000, 2, 5, 100, false).disponivel());
        assertFalse(new ItemPedido("SKU-1", 1_000, 5, 2, 100, false).disponivel());
    }
}
