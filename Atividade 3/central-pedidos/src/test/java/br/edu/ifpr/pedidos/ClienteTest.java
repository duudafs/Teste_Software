package br.edu.ifpr.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void deveCriarClienteEValidarHistorico() {
        Cliente cliente = new Cliente(true, false, 5);

        assertAll(
            () -> assertTrue(cliente.vip()),
            () -> assertFalse(cliente.bloqueado()),
            () -> assertEquals(5, cliente.comprasAnteriores())
        );
        assertThrows(IllegalArgumentException.class, () -> new Cliente(false, false, -1));
    }
}
