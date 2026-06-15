package com.riva.model.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.riva.pattern.notification.CanalEmail;
import com.riva.pattern.notification.CanalNotificacion;
import com.riva.pattern.notification.CanalPush;
import com.riva.pattern.notification.CanalSMS;

class ClienteTest {

    @Test
    void clienteNuevoTieneTodosLosCanalesHabilitadosPorDefecto() {
        Cliente cliente = new Cliente("Ana", "Diaz", "ana@riva.com", "hash");

        assertThat(cliente.canalesNotificacionHabilitados())
                .hasSize(3)
                .hasAtLeastOneElementOfType(CanalEmail.class)
                .hasAtLeastOneElementOfType(CanalSMS.class)
                .hasAtLeastOneElementOfType(CanalPush.class);
    }

    @Test
    void canalEmailUsaElContactoRealDelCliente() {
        Cliente cliente = new Cliente("Ana", "Diaz", "ANA@riva.com", "hash");

        CanalEmail email = (CanalEmail) cliente.canalesNotificacionHabilitados().stream()
                .filter(canal -> canal instanceof CanalEmail)
                .findFirst()
                .orElseThrow();

        assertThat(email.getEmail()).isEqualTo("ana@riva.com");
    }

    @Test
    void desactivarCanalesQuitaLosCanalesCorrespondientes() {
        Cliente cliente = new Cliente("Ana", "Diaz", "ana@riva.com", "hash");

        cliente.configurarNotificaciones(true, false, false);

        assertThat(cliente.canalesNotificacionHabilitados())
                .hasSize(1)
                .allMatch(CanalEmail.class::isInstance);
    }

    @Test
    void sinCanalesHabilitadosNoSuscribeNinguno() {
        Cliente cliente = new Cliente("Ana", "Diaz", "ana@riva.com", "hash");

        cliente.configurarNotificaciones(false, false, false);

        assertThat(cliente.canalesNotificacionHabilitados()).isEmpty();
    }

    @Test
    void canalSmsYPushSeConstruyenAunqueElDestinoSeaSimulado() {
        Cliente cliente = new Cliente("Ana", "Diaz", "ana@riva.com", "hash");
        cliente.configurarNotificaciones(false, true, true);

        var canales = cliente.canalesNotificacionHabilitados();

        assertThat(canales)
                .hasSize(2)
                .hasAtLeastOneElementOfType(CanalSMS.class)
                .hasAtLeastOneElementOfType(CanalPush.class)
                .allSatisfy(canal -> assertThat(canal).isInstanceOf(CanalNotificacion.class));
    }
}
