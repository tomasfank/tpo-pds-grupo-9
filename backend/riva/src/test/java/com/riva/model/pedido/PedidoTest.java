package com.riva.model.pedido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.pattern.state.EstadoEntregado;
import com.riva.pattern.state.EstadoEnviado;
import com.riva.pattern.state.EstadoPagado;
import com.riva.pattern.state.EstadoPendiente;

class PedidoTest {

    @Test
    void nuevoPedidoIniciaEnEstadoPendienteYRegistraHistorialInicial() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);

        assertThat(pedido.getEstado()).isInstanceOf(EstadoPendiente.class);
        assertThat(pedido.nombreEstadoActual()).isEqualTo("Pendiente");
        assertThat(pedido.getHistorialEstados()).hasSize(1);
        assertThat(pedido.getHistorialEstados().getFirst().getEstado()).isEqualTo("Pendiente");
    }

    @Test
    void avanzarEstadoRecorreCadenaStateDelUmlYRegistraCadaTransicion() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);

        pedido.avanzarEstado();
        assertThat(pedido.getEstado()).isInstanceOf(EstadoPagado.class);
        assertThat(pedido.nombreEstadoActual()).isEqualTo("Pagado");

        pedido.avanzarEstado();
        assertThat(pedido.getEstado()).isInstanceOf(EstadoEnviado.class);
        assertThat(pedido.nombreEstadoActual()).isEqualTo("Enviado");

        pedido.avanzarEstado();
        assertThat(pedido.getEstado()).isInstanceOf(EstadoEntregado.class);
        assertThat(pedido.nombreEstadoActual()).isEqualTo("Entregado");

        assertThat(pedido.getHistorialEstados())
                .extracting(TransicionEstado::getEstado)
                .containsExactly("Pendiente", "Pagado", "Enviado", "Entregado");
    }

    @Test
    void estadoEntregadoEsFinalYNoPermiteAvanzar() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        pedido.avanzarEstado();
        pedido.avanzarEstado();
        pedido.avanzarEstado();

        assertThatThrownBy(pedido::avanzarEstado)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado final");
        assertThat(pedido.nombreEstadoActual()).isEqualTo("Entregado");
        assertThat(pedido.getHistorialEstados()).hasSize(4);
    }

    @Test
    void itemPedidoCongelaPrecioUnitarioYCalculaSubtotal() {
        ItemPedido item = itemPedido();

        assertThat(item.subtotal()).isEqualTo(250);
        assertThat(item.getPrecioUnitario()).isEqualByComparingTo("125");
    }

    private static ItemPedido itemPedido() {
        ProductVariant variante = new ProductVariant(
                "var-1",
                Size.M,
                "Negro",
                4,
                BigDecimal.valueOf(125),
                "prod-1",
                "Remera"
        );
        return ItemPedido.desdeVariante(variante, 2);
    }
}
