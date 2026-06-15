package com.riva.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfiguracionTest {

    @Test
    void obtenerInstanciaDevuelveSiempreLaMismaReferencia() {
        // Patron Singleton: una unica instancia compartida.
        assertThat(Configuracion.obtenerInstancia()).isSameAs(Configuracion.obtenerInstancia());
    }

    @Test
    void exponeLosParametrosGeneralesDelEcommerce() {
        Configuracion config = Configuracion.obtenerInstancia();

        assertThat(config.getMoneda()).isEqualTo("USD");
        assertThat(config.getTasaIva()).isEqualTo(0.21);
        assertThat(config.getCostoEnvio()).isEqualTo(9.99);
        assertThat(config.getUmbralEnvioGratis()).isEqualTo(100.0);
    }

    @Test
    void actualizarParametroGuardaYRecuperaPorClave() {
        // Documenta el almacen generico clave-valor del Singleton: punto de extension para
        // sumar parametros en runtime sin tocar la API tipada. No tiene endpoint REST porque
        // editar configuracion no es un caso de uso del alcance (ver javadoc de Configuracion).
        Configuracion config = Configuracion.obtenerInstancia();

        config.actualizarParametro("clave-demo", "valor-demo");

        assertThat(config.obtenerParametro("clave-demo")).isEqualTo("valor-demo");
    }
}
