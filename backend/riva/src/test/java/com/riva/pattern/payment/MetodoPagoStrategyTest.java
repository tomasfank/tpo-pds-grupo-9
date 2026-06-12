package com.riva.pattern.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetodoPagoStrategyTest {

    @Test
    void pagoTarjetaValidaDatosYRechazaNumeroTerminadoEn0000() {
        PagoTarjeta aprobado = new PagoTarjeta(
                "4111111111111111",
                "Guido Morabito",
                "12/29",
                "123",
                new AdapterTarjeta(new PasarelaTarjetaExterna())
        );
        PagoTarjeta rechazado = new PagoTarjeta(
                "4111111111110000",
                "Guido Morabito",
                "12/29",
                "123",
                new AdapterTarjeta(new PasarelaTarjetaExterna())
        );

        assertThat(aprobado.validarDatosPago()).isTrue();
        assertThat(aprobado.procesarPago(100).exito()).isTrue();
        assertThat(rechazado.validarDatosPago()).isTrue();
        assertThat(rechazado.procesarPago(100).exito()).isFalse();
    }

    @Test
    void pagoTarjetaInvalidaNumeroCorto() {
        PagoTarjeta pago = new PagoTarjeta(
                "4111",
                "Guido Morabito",
                "12/29",
                "123",
                new AdapterTarjeta(new PasarelaTarjetaExterna())
        );

        assertThat(pago.validarDatosPago()).isFalse();
    }

    @Test
    void pagoPayPalValidaEmailYRechazaDominioDePrueba() {
        PagoPayPal aprobado = new PagoPayPal(
                "cliente@paypal.test",
                new AdapterPayPal(new ServicioPayPalExterno())
        );
        PagoPayPal rechazado = new PagoPayPal(
                "cliente@rechazado.test",
                new AdapterPayPal(new ServicioPayPalExterno())
        );

        assertThat(aprobado.validarDatosPago()).isTrue();
        assertThat(aprobado.procesarPago(100).exito()).isTrue();
        assertThat(rechazado.validarDatosPago()).isTrue();
        assertThat(rechazado.procesarPago(100).exito()).isFalse();
    }

    @Test
    void pagoPayPalInvalidaEmailSinDominio() {
        PagoPayPal pago = new PagoPayPal(
                "cliente",
                new AdapterPayPal(new ServicioPayPalExterno())
        );

        assertThat(pago.validarDatosPago()).isFalse();
    }

    @Test
    void pagoTransferenciaValidaCbuOAliasYRechazaAliasRechazada() {
        PagoTransferencia conCbu = new PagoTransferencia(
                "0000003100010000000001",
                null,
                "Banco Demo",
                new AdapterTransferencia(new ServicioBancoExterno())
        );
        PagoTransferencia conAliasRechazado = new PagoTransferencia(
                null,
                "rechazada",
                "Banco Demo",
                new AdapterTransferencia(new ServicioBancoExterno())
        );

        assertThat(conCbu.validarDatosPago()).isTrue();
        assertThat(conCbu.procesarPago(100).exito()).isTrue();
        assertThat(conAliasRechazado.validarDatosPago()).isTrue();
        assertThat(conAliasRechazado.procesarPago(100).exito()).isFalse();
    }

    @Test
    void pagoTransferenciaInvalidaSiNoTieneCbuNiAlias() {
        PagoTransferencia pago = new PagoTransferencia(
                null,
                null,
                "Banco Demo",
                new AdapterTransferencia(new ServicioBancoExterno())
        );

        assertThat(pago.validarDatosPago()).isFalse();
    }
}
