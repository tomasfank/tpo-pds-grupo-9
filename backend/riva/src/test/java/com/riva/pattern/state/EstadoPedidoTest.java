package com.riva.pattern.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

class EstadoPedidoTest {

    @Test
    void estadosConcretosExponenNombreYPuedeAvanzarSegunUml() {
        assertThat(new EstadoPendiente().nombre()).isEqualTo("Pendiente");
        assertThat(new EstadoPendiente().puedeAvanzar()).isTrue();
        assertThat(new EstadoPagado().nombre()).isEqualTo("Pagado");
        assertThat(new EstadoPagado().puedeAvanzar()).isTrue();
        assertThat(new EstadoEnviado().nombre()).isEqualTo("Enviado");
        assertThat(new EstadoEnviado().puedeAvanzar()).isTrue();
        assertThat(new EstadoEntregado().nombre()).isEqualTo("Entregado");
        assertThat(new EstadoEntregado().puedeAvanzar()).isFalse();
    }

    @Test
    void cadaEstadoConcretoDefineSuSiguienteEstado() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);

        new EstadoPendiente().avanzar(pedido);
        assertThat(pedido.getEstado()).isInstanceOf(EstadoPagado.class);

        new EstadoPagado().avanzar(pedido);
        assertThat(pedido.getEstado()).isInstanceOf(EstadoEnviado.class);

        new EstadoEnviado().avanzar(pedido);
        assertThat(pedido.getEstado()).isInstanceOf(EstadoEntregado.class);
    }

    @Test
    void estadoEntregadoNoAvanzaPorqueEsEstadoFinal() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);

        assertThatThrownBy(() -> new EstadoEntregado().avanzar(pedido))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado final");
    }

    private static ItemPedido itemPedido() {
        ProductVariant variante = new ProductVariant(
                "var-1",
                Size.M,
                "Negro",
                4,
                BigDecimal.valueOf(100),
                "prod-1",
                "Remera"
        );
        return ItemPedido.desdeVariante(variante, 1);
    }
}
