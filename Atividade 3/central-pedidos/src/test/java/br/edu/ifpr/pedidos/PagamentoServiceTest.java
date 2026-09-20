package br.edu.ifpr.pedidos;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PagamentoServiceTest {

    @Test
    void deveValidarTotalEQuantidadeDeTentativas() {
        PagamentoService service = new PagamentoService(total -> true);

        assertThrows(IllegalArgumentException.class, () -> service.pagar(0, 1));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(-100, 1));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 0));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(1_000, 4));
    }

    @Test
    void deveAprovarOuRecusarNaPrimeiraTentativaSemRepetir() {
        List<Long> aprovacao = new ArrayList<>();
        PagamentoService aprova = new PagamentoService(total -> {
            aprovacao.add(total);
            return true;
        });
        assertTrue(aprova.pagar(11_200, 3));
        assertEquals(List.of(11_200L), aprovacao);

        List<Long> recusa = new ArrayList<>();
        PagamentoService recusaService = new PagamentoService(total -> {
            recusa.add(total);
            return false;
        });
        assertFalse(recusaService.pagar(11_200, 3));
        assertEquals(1, recusa.size());
    }

    @Test
    void deveRepetirAposIndisponibilidadeAteAprovarOuEsgotarTentativas() {
        int[] contador = {0};
        PagamentoService aprovaNaSegunda = new PagamentoService(total -> {
            contador[0]++;
            if (contador[0] < 2) throw new IllegalStateException("indisponivel");
            return true;
        });
        assertTrue(aprovaNaSegunda.pagar(5_000, 3));
        assertEquals(2, contador[0]);

        int[] contadorEsgotado = {0};
        PagamentoService sempreIndisponivel = new PagamentoService(total -> {
            contadorEsgotado[0]++;
            throw new IllegalStateException("indisponivel");
        });
        assertFalse(sempreIndisponivel.pagar(5_000, 3));
        assertEquals(3, contadorEsgotado[0]);
    }

    @Test
    void devePropagarOutrasExcecoesSemRepetir() {
        int[] contador = {0};
        PagamentoService service = new PagamentoService(total -> {
            contador[0]++;
            throw new RuntimeException("falha grave");
        });

        assertThrows(RuntimeException.class, () -> service.pagar(5_000, 3));
        assertEquals(1, contador[0]);
    }
}
