package br.edu.ifpr.pedidos;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraFreteTest {

    private final CalculadoraFrete frete = new CalculadoraFrete();
    private final Cliente comum = new Cliente(false, false, 1);
    private final Cliente vip = new Cliente(true, false, 1);

    private Pedido pedidoComPeso(int pesoGramas, String uf, boolean expresso, boolean fragilAtivo) {
        ItemPedido item = new ItemPedido("SKU-1", 1_000, 1, 5, pesoGramas, fragilAtivo);
        return new Pedido(List.of(item), uf, expresso, null);
    }

    @Test
    void deveRejeitarLiquidoNegativoEAplicarBaseConformeUf() {
        Pedido pedido = pedidoComPeso(100, "PR", false, false);
        assertThrows(IllegalArgumentException.class, () -> frete.calcular(pedido, comum, -1));

        assertEquals(1_200L, frete.calcular(pedidoComPeso(100, "PR", false, false), comum, 1_000));
        assertEquals(2_000L, frete.calcular(pedidoComPeso(100, "SP", false, false), comum, 1_000));
        assertEquals(2_000L, frete.calcular(pedidoComPeso(100, "RJ", false, false), comum, 1_000));
        assertEquals(3_000L, frete.calcular(pedidoComPeso(100, "BA", false, false), comum, 1_000));
    }

    @Test
    void deveCobrarAdicionalDePesoPorFracaoDeQuilo() {
        assertEquals(1_200L, frete.calcular(pedidoComPeso(2_000, "PR", false, false), comum, 1_000));
        assertEquals(1_500L, frete.calcular(pedidoComPeso(2_001, "PR", false, false), comum, 1_000));
        assertEquals(1_500L, frete.calcular(pedidoComPeso(3_000, "PR", false, false), comum, 1_000));
        assertEquals(1_800L, frete.calcular(pedidoComPeso(3_001, "PR", false, false), comum, 1_000));
    }

    @Test
    void deveZerarFreteApenasComEntregaNormalEAplicarMetadeParaVip() {
        assertEquals(0L, frete.calcular(pedidoComPeso(3_000, "PR", false, false), comum, 30_000));
        assertEquals(2_700L, frete.calcular(pedidoComPeso(100, "PR", true, false), comum, 30_000));
        assertEquals(600L, frete.calcular(pedidoComPeso(100, "PR", false, false), vip, 1_000));
        assertEquals(0L, frete.calcular(pedidoComPeso(100, "PR", false, false), vip, 30_000));
    }

    @Test
    void expressoEFragilAcrescentamAdicionaisInclusiveComBaseZerada() {
        assertEquals(2_700L, frete.calcular(pedidoComPeso(100, "PR", true, false), comum, 1_000));
        assertEquals(1_700L, frete.calcular(pedidoComPeso(100, "PR", false, true), comum, 1_000));
        assertEquals(500L, frete.calcular(pedidoComPeso(100, "PR", false, true), comum, 30_000));
    }
}
