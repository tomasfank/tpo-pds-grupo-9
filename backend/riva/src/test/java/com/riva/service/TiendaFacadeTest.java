package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.riva.dto.PedidoResponse;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

@ExtendWith(MockitoExtension.class)
class TiendaFacadeTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private TiendaFacade tiendaFacade;

    @Test
    void confirmarCompraOrquestaCrearPedidoYProcesarPago() {
        Pedido creado = pedido(2); // total 200
        when(pedidoService.crearDesdeCarrito("cliente-1", null)).thenReturn(creado);
        when(pedidoService.procesarPago(eq("cliente-1"), any(), eq(pago())))
                .thenReturn(new ProcesarPagoResponse(true, "Pago aprobado", PedidoResponse.from(pedido(2))));

        var response = tiendaFacade.confirmarCompra("cliente-1", pago(), null);

        // Facade: primero crea el pedido y luego procesa el pago (sin exponer los pasos).
        verify(pedidoService).crearDesdeCarrito("cliente-1", null);
        verify(pedidoService).procesarPago(eq("cliente-1"), any(), eq(pago()));
        assertThat(response.exito()).isTrue();
    }

    @Test
    void totalSobreElUmbralAplicaEnvioGratis() {
        when(pedidoService.crearDesdeCarrito("cliente-1", null)).thenReturn(pedido(2));
        when(pedidoService.procesarPago(eq("cliente-1"), any(), eq(pago())))
                .thenReturn(new ProcesarPagoResponse(true, "Pago aprobado", PedidoResponse.from(pedido(2))));

        var response = tiendaFacade.confirmarCompra("cliente-1", pago(), null);

        // total 200 >= umbral 100 (Singleton Configuracion) -> envio gratis.
        assertThat(response.envioGratis()).isTrue();
        assertThat(response.costoEnvio()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void totalBajoElUmbralCobraElCostoDeEnvio() {
        when(pedidoService.crearDesdeCarrito("cliente-1", null)).thenReturn(pedidoBarato());
        when(pedidoService.procesarPago(eq("cliente-1"), any(), eq(pago())))
                .thenReturn(new ProcesarPagoResponse(true, "Pago aprobado", PedidoResponse.from(pedidoBarato())));

        var response = tiendaFacade.confirmarCompra("cliente-1", pago(), null);

        // total 30 < umbral 100 -> se cobra el costo de envio del Singleton (9.99).
        assertThat(response.envioGratis()).isFalse();
        assertThat(response.costoEnvio()).isEqualByComparingTo(BigDecimal.valueOf(9.99));
    }

    private static ProcesarPagoRequest pago() {
        return new ProcesarPagoRequest(
                "TARJETA", "4111111111111111", "Guido Morabito", "12/29", "123",
                null, null, null, null);
    }

    private static Pedido pedido(int cantidad) {
        ProductVariant variante = new ProductVariant(
                "var-1", Size.M, "Negro", 10, BigDecimal.valueOf(100), "prod-1", "Remera");
        return new Pedido("cliente-1", List.of(ItemPedido.desdeVariante(variante, cantidad)), null);
    }

    private static Pedido pedidoBarato() {
        ProductVariant variante = new ProductVariant(
                "var-2", Size.S, "Azul", 10, BigDecimal.valueOf(30), "prod-2", "Medias");
        return new Pedido("cliente-1", List.of(ItemPedido.desdeVariante(variante, 1)), null);
    }
}
