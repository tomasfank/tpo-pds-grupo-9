package com.riva.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.riva.dto.DireccionEnvioRequest;
import com.riva.dto.PedidoResponse;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.model.pedido.DireccionEnvio;
import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.model.user.Rol;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.PedidoService;

class PedidoControllerTest {

    private final PedidoService pedidoService = mock(PedidoService.class);
    private final PedidoController controller = new PedidoController(pedidoService);

    @Test
    void crearCreaPedidoDesdeCarritoDelClienteYDevuelveEstadoPendiente() {
        Pedido pedido = pedido();
        when(pedidoService.crearDesdeCarrito("cliente-1", null)).thenReturn(pedido);

        PedidoResponse response = controller.crear(principal(), null);

        assertThat(response.estado()).isEqualTo("Pendiente");
        assertThat(response.items()).hasSize(1);
        assertThat(response.historialEstados()).extracting("estado").containsExactly("Pendiente");
    }

    @Test
    void listarDevuelvePedidosDelCliente() {
        when(pedidoService.listarDelCliente("cliente-1")).thenReturn(List.of(pedido()));

        List<PedidoResponse> response = controller.listar(principal());

        verify(pedidoService).listarDelCliente("cliente-1");
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().clienteId()).isEqualTo("cliente-1");
    }

    @Test
    void detalleDevuelvePedidoDelCliente() {
        when(pedidoService.obtenerDetalle("cliente-1", "pedido-1")).thenReturn(pedido());

        PedidoResponse response = controller.detalle(principal(), "pedido-1");

        verify(pedidoService).obtenerDetalle("cliente-1", "pedido-1");
        assertThat(response.estado()).isEqualTo("Pendiente");
    }

    @Test
    void avanzarDelegaEnServicioYDevuelveEstadoActualizado() {
        Pedido pedido = pedido();
        pedido.avanzarEstado();
        when(pedidoService.avanzarEstado("pedido-1")).thenReturn(pedido);

        PedidoResponse response = controller.avanzar("pedido-1");

        verify(pedidoService).avanzarEstado("pedido-1");
        assertThat(response.estado()).isEqualTo("Pagado");
    }

    @Test
    void actualizarDireccionDelegaEnServicioYDevuelvePedidoConEnvio() {
        DireccionEnvioRequest request = new DireccionEnvioRequest(
                "Av. Corrientes",
                "1234",
                "CABA",
                "Buenos Aires",
                "C1043"
        );
        when(pedidoService.actualizarDireccionEnvio(eq("cliente-1"), eq("pedido-1"), any(DireccionEnvio.class)))
                .thenReturn(pedidoConDireccion());

        PedidoResponse response = controller.actualizarDireccion(principal(), "pedido-1", request);

        assertThat(response.direccionEnvio().calle()).isEqualTo("Av. Corrientes");
        assertThat(response.direccionEnvio().codigoPostal()).isEqualTo("C1043");
    }

    @Test
    void procesarPagoDelegaEnServicioYDevuelveResultadoConPedidoPagado() {
        Pedido pedido = pedido();
        pedido.registrarMetodoPago("Tarjeta");
        pedido.avanzarEstado();
        ProcesarPagoRequest request = new ProcesarPagoRequest(
                "TARJETA",
                "4111111111111111",
                "Guido Morabito",
                "12/29",
                "123",
                null,
                null,
                null,
                null
        );
        when(pedidoService.procesarPago("cliente-1", "pedido-1", request))
                .thenReturn(new ProcesarPagoResponse(true, "Pago aprobado", PedidoResponse.from(pedido)));

        ProcesarPagoResponse response = controller.procesarPago(principal(), "pedido-1", request);

        verify(pedidoService).procesarPago("cliente-1", "pedido-1", request);
        assertThat(response.exito()).isTrue();
        assertThat(response.pedido().estado()).isEqualTo("Pagado");
        assertThat(response.pedido().metodoPagoNombre()).isEqualTo("Tarjeta");
    }

    private static UsuarioPrincipal principal() {
        return new UsuarioPrincipal("cliente-1", "cliente-1@riva.com", "hash", Rol.CLIENTE);
    }

    private static Pedido pedido() {
        return new Pedido("cliente-1", List.of(itemPedido()), null);
    }

    private static Pedido pedidoConDireccion() {
        return new Pedido(
                "cliente-1",
                List.of(itemPedido()),
                new DireccionEnvio("Av. Corrientes", "1234", "CABA", "Buenos Aires", "C1043")
        );
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
        return ItemPedido.desdeVariante(variante, 2);
    }
}
