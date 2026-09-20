package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {

    private ItemPedido itemAtivo(long preco, int quantidade, int peso, boolean fragil) {
        return new ItemPedido("SKU-1", preco, quantidade, quantidade + 10, peso, fragil);
    }

    @Test
    void deveValidarListaDeItensEUf() {
        assertThrows(IllegalArgumentException.class, () -> new Pedido(null, "PR", false, null));

        List<ItemPedido> cento = new ArrayList<>();
        for (int i = 0; i < 100; i++) cento.add(new ItemPedido("SKU-" + i, 100, 1, 5, 100, false));
        assertEquals(100, new Pedido(cento, "PR", false, null).itens().size());

        List<ItemPedido> centoEUm = new ArrayList<>(cento);
        centoEUm.add(new ItemPedido("SKU-EXTRA", 100, 1, 5, 100, false));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(centoEUm, "PR", false, null));

        List<ItemPedido> comNulo = new ArrayList<>();
        comNulo.add(null);
        assertThrows(NullPointerException.class, () -> new Pedido(comNulo, "PR", false, null));

        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), null, false, null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "P", false, null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(List.of(), "pr", false, null));
        assertEquals("BA", new Pedido(List.of(), "BA", false, null).uf());
    }

    @Test
    void deveCopiarListaDefensivamente() {
        List<ItemPedido> itens = new ArrayList<>();
        itens.add(itemAtivo(1_000, 1, 100, false));
        Pedido pedido = new Pedido(itens, "PR", false, null);

        itens.add(itemAtivo(5_000, 1, 100, false));

        assertEquals(1, pedido.itens().size());
    }

    @Test
    void deveCalcularSubtotalPesoEFragilidadeConsiderandoApenasAtivos() {
        ItemPedido ativo = itemAtivo(1_000, 2, 300, false);
        ItemPedido inativo = itemAtivo(5_000, 0, 999, false);
        ItemPedido fragilAtivo = itemAtivo(1_000, 1, 100, true);
        ItemPedido fragilInativo = itemAtivo(1_000, 0, 100, true);

        Pedido pedido = new Pedido(List.of(ativo, inativo), "PR", false, null);
        assertAll(
            () -> assertEquals(2_000L, pedido.subtotalCentavos()),
            () -> assertEquals(600, pedido.pesoGramas()),
            () -> assertEquals(0L, new Pedido(List.of(), "PR", false, null).subtotalCentavos())
        );
        assertTrue(new Pedido(List.of(fragilAtivo), "PR", false, null).temFragil());
        assertFalse(new Pedido(List.of(fragilInativo), "PR", false, null).temFragil());
    }

    @Test
    void deveDetectarFaltaDeEstoqueEmQualquerPosicao() {
        ItemPedido semEstoque = new ItemPedido("SKU-1", 1_000, 5, 1, 100, false);
        ItemPedido comEstoque = new ItemPedido("SKU-2", 1_000, 1, 5, 100, false);

        assertTrue(new Pedido(List.of(comEstoque), "PR", false, null).estoqueSuficiente());
        assertFalse(new Pedido(List.of(semEstoque, comEstoque), "PR", false, null).estoqueSuficiente());
        assertFalse(new Pedido(List.of(comEstoque, semEstoque), "PR", false, null).estoqueSuficiente());
    }
}
