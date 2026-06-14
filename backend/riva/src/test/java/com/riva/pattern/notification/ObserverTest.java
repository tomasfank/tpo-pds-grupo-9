package com.riva.pattern.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

class ObserverTest {

    @Test
    void notificarActualizaATodosLosCanalesSuscriptos() {
        Pedido pedido = pedido();
        CanalNotificacion email = mock(CanalNotificacion.class);
        CanalNotificacion sms = mock(CanalNotificacion.class);
        CanalNotificacion push = mock(CanalNotificacion.class);
        pedido.suscribir(email);
        pedido.suscribir(sms);
        pedido.suscribir(push);

        pedido.notificar();

        verify(email).actualizar(pedido);
        verify(sms).actualizar(pedido);
        verify(push).actualizar(pedido);
    }

    @Test
    void desuscribirEvitaQueElCanalRecibaNotificaciones() {
        Pedido pedido = pedido();
        CanalNotificacion email = mock(CanalNotificacion.class);
        pedido.suscribir(email);
        pedido.desuscribir(email);

        pedido.notificar();

        verify(email, never()).actualizar(pedido);
    }

    @Test
    void suscribirRechazaCanalNulo() {
        Pedido pedido = pedido();

        assertThatThrownBy(() -> pedido.suscribir(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("canal es obligatorio");
    }

    @Test
    void canalesConcretosConservanSusDatosDeDestino() {
        CanalEmail email = new CanalEmail("cliente@riva.com");
        CanalSMS sms = new CanalSMS("1122334455");
        CanalPush push = new CanalPush("device-token");

        assertThat(email.getEmail()).isEqualTo("cliente@riva.com");
        assertThat(sms.getNumeroTelefono()).isEqualTo("1122334455");
        assertThat(push.getDeviceToken()).isEqualTo("device-token");
    }

    @Test
    void canalesConcretosRechazanPedidoNulo() {
        assertThatThrownBy(() -> new CanalEmail("cliente@riva.com").actualizar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedido es obligatorio");
        assertThatThrownBy(() -> new CanalSMS("1122334455").actualizar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedido es obligatorio");
        assertThatThrownBy(() -> new CanalPush("device-token").actualizar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pedido es obligatorio");
    }

    @Test
    void avanzarEstadoNotificaAutomaticamenteALosCanalesSuscriptos() {
        Pedido pedido = pedido();
        CanalNotificacion canal = mock(CanalNotificacion.class);
        pedido.suscribir(canal);

        pedido.avanzarEstado();

        assertThat(pedido.nombreEstadoActual()).isEqualTo("Pagado");
        verify(canal).actualizar(pedido);
    }

    @Test
    void preferenciasPermitenHabilitarYDeshabilitarCanales() {
        PreferenciasNotificacion preferencias =
                new PreferenciasNotificacion(true, false, false);

        preferencias.configurarEmail(false);
        preferencias.configurarSms(true);
        preferencias.configurarPush(true);

        assertThat(preferencias.isEmail()).isFalse();
        assertThat(preferencias.isSms()).isTrue();
        assertThat(preferencias.isPush()).isTrue();
    }

    private static Pedido pedido() {
        ProductVariant variante = new ProductVariant(
                "var-1",
                Size.M,
                "Negro",
                4,
                BigDecimal.valueOf(100),
                "prod-1",
                "Remera"
        );
        return new Pedido("cliente-1", List.of(ItemPedido.desdeVariante(variante, 1)), null);
    }
}
