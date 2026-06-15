package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.model.cart.Carrito;
import com.riva.model.pedido.DireccionEnvio;
import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.model.user.Cliente;
import com.riva.pattern.notification.CanalNotificacion;
import com.riva.pattern.payment.MetodoPagoFactory;
import com.riva.pattern.state.EstadoPagado;
import com.riva.repository.CarritoRepository;
import com.riva.repository.PedidoRepository;
import com.riva.repository.ProductRepository;
import com.riva.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private MetodoPagoFactory metodoPagoFactory = new MetodoPagoFactory();

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void crearDesdeCarritoCopiaItemsCongelaPrecioYPersistePedidoPendiente() {
        Carrito carrito = carritoConItem("cliente-1");
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido pedido = pedidoService.crearDesdeCarrito("cliente-1", null);

        assertThat(pedido.getClienteId()).isEqualTo("cliente-1");
        assertThat(pedido.nombreEstadoActual()).isEqualTo("Pendiente");
        assertThat(pedido.getItems()).hasSize(1);
        assertThat(pedido.getItems().getFirst().getPrecioUnitario()).isEqualByComparingTo("100");
        assertThat(pedido.getTotal()).isEqualByComparingTo("200");
        assertThat(pedido.getHistorialEstados()).extracting("estado").containsExactly("Pendiente");
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void crearDesdeCarritoVacioFalla() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(new Carrito("cliente-1")));

        assertThatThrownBy(() -> pedidoService.crearDesdeCarrito("cliente-1", null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("carrito esta vacio");
    }

    @Test
    void listarDelClienteDevuelveSoloPedidosPropiosOrdenadosPorFechaDescendente() {
        when(pedidoRepository.findByClienteIdOrderByFechaDesc("cliente-1")).thenReturn(List.of());

        assertThat(pedidoService.listarDelCliente("cliente-1")).isEmpty();

        verify(pedidoRepository).findByClienteIdOrderByFechaDesc("cliente-1");
    }

    @Test
    void obtenerDetalleRechazaPedidoDeOtroCliente() {
        Pedido pedido = new Pedido("otro-cliente", List.of(itemPedido()), null);
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.obtenerDetalle("cliente-1", "pedido-1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Pedido no encontrado");
    }

    @Test
    void avanzarEstadoDelegaEnPedidoYPersisteNuevoEstado() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido actualizado = pedidoService.avanzarEstado("pedido-1");

        assertThat(actualizado.getEstado()).isInstanceOf(EstadoPagado.class);
        assertThat(actualizado.getHistorialEstados()).extracting("estado").containsExactly("Pendiente", "Pagado");
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertThat(captor.getValue().nombreEstadoActual()).isEqualTo("Pagado");
    }

    @Test
    void actualizarDireccionEnvioPersisteDireccionSiPedidoTodaviaNoFueEnviado() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        DireccionEnvio direccion = direccion();
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido actualizado = pedidoService.actualizarDireccionEnvio("cliente-1", "pedido-1", direccion);

        assertThat(actualizado.getDireccionEnvio().getCalle()).isEqualTo("Av. Corrientes");
        assertThat(actualizado.getDireccionEnvio().getCodigoPostal()).isEqualTo("C1043");
        verify(pedidoRepository).save(actualizado);
    }

    @Test
    void actualizarDireccionEnvioRechazaPedidoEnviado() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        pedido.avanzarEstado();
        pedido.avanzarEstado();
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.actualizarDireccionEnvio("cliente-1", "pedido-1", direccion()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("direccion no puede modificarse");
    }

    @Test
    void procesarPagoExitosoAvanzaADPagadoDescuentaStockYVaciaCarrito() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        Product product = productoConStock(4);
        Carrito carrito = carritoConItem("cliente-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcesarPagoResponse response = pedidoService.procesarPago(
                "cliente-1",
                "pedido-1",
                requestTarjeta("4111111111111111")
        );

        assertThat(response.exito()).isTrue();
        assertThat(response.pedido().estado()).isEqualTo("Pagado");
        assertThat(response.pedido().metodoPagoNombre()).isEqualTo("Tarjeta");
        assertThat(product.getVariants().getFirst().getStock()).isEqualTo(3);
        assertThat(carrito.estaVacio()).isTrue();
    }

    @Test
    void procesarPagoFallidoMantienePedidoPendienteSinDescontarStockNiVaciarCarrito() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        Product product = productoConStock(4);
        Carrito carrito = carritoConItem("cliente-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));

        ProcesarPagoResponse response = pedidoService.procesarPago(
                "cliente-1",
                "pedido-1",
                requestTarjeta("4111111111110000")
        );

        assertThat(response.exito()).isFalse();
        assertThat(response.pedido().estado()).isEqualTo("Pendiente");
        assertThat(product.getVariants().getFirst().getStock()).isEqualTo(4);
        assertThat(carrito.estaVacio()).isFalse();
    }

    @Test
    void procesarPagoConStockInsuficienteNoInvocaPagoYMantienePedidoPendiente() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        Product product = productoConStock(0);
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));

        ProcesarPagoResponse response = pedidoService.procesarPago(
                "cliente-1",
                "pedido-1",
                requestTarjeta("4111111111111111")
        );

        assertThat(response.exito()).isFalse();
        assertThat(response.mensaje()).contains("Stock insuficiente");
        assertThat(response.pedido().estado()).isEqualTo("Pendiente");
        assertThat(product.getVariants().getFirst().getStock()).isZero();
    }

    @Test
    void procesarPagoRechazaPedidoNoPendiente() {
        Pedido pedido = new Pedido("cliente-1", List.of(itemPedido()), null);
        pedido.avanzarEstado();
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.procesarPago("cliente-1", "pedido-1", requestTarjeta("4111111111111111")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Solo se pueden pagar pedidos pendientes");
    }

    @Test
    void avanzarEstadoSuscribeLosCanalesHabilitadosDelClienteAntesDeNotificar() {
        Pedido pedido = spy(new Pedido("cliente-1", List.of(itemPedido()), null));
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente(true, true, true)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pedido actualizado = pedidoService.avanzarEstado("pedido-1");

        // Observer: los 3 canales habilitados quedan suscriptos al pedido y la
        // transicion (que dispara notificar()) se completa.
        verify(pedido, times(3)).suscribir(any(CanalNotificacion.class));
        assertThat(actualizado.nombreEstadoActual()).isEqualTo("Pagado");
    }

    @Test
    void avanzarEstadoSoloSuscribeLosCanalesQueElClienteDejoHabilitados() {
        Pedido pedido = spy(new Pedido("cliente-1", List.of(itemPedido()), null));
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente(true, false, false)));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pedidoService.avanzarEstado("pedido-1");

        verify(pedido, times(1)).suscribir(any(CanalNotificacion.class));
    }

    @Test
    void procesarPagoExitosoSuscribeLosCanalesDelClienteParaNotificarElPago() {
        Pedido pedido = spy(new Pedido("cliente-1", List.of(itemPedido()), null));
        Product product = productoConStock(4);
        Carrito carrito = carritoConItem("cliente-1");
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findById("cliente-1")).thenReturn(Optional.of(cliente(true, true, true)));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcesarPagoResponse response = pedidoService.procesarPago(
                "cliente-1", "pedido-1", requestTarjeta("4111111111111111"));

        assertThat(response.exito()).isTrue();
        verify(pedido, times(3)).suscribir(any(CanalNotificacion.class));
    }

    private static Cliente cliente(boolean email, boolean sms, boolean push) {
        Cliente cliente = new Cliente("Cliente", "Demo", "cliente@riva.com", "hash");
        cliente.configurarNotificaciones(email, sms, push);
        return cliente;
    }

    private static Carrito carritoConItem(String clienteId) {
        Carrito carrito = new Carrito(clienteId);
        carrito.agregarItem(variante(), 2);
        return carrito;
    }

    private static ItemPedido itemPedido() {
        return ItemPedido.desdeVariante(variante(), 1);
    }

    private static ProductVariant variante() {
        return new ProductVariant(
                "var-1",
                Size.M,
                "Negro",
                3,
                BigDecimal.valueOf(100),
                "prod-1",
                "Remera"
        );
    }

    private static Product productoConStock(int stock) {
        return new Product(
                "Remera",
                "Remera basica",
                BigDecimal.valueOf(100),
                "Algodon",
                "cat-1",
                List.of("cat-1"),
                List.of(new ProductVariant("var-1", Size.M, "Negro", stock)),
                List.of("https://example.com/remera.jpg")
        );
    }

    private static ProcesarPagoRequest requestTarjeta(String numeroTarjeta) {
        return new ProcesarPagoRequest(
                "TARJETA",
                numeroTarjeta,
                "Guido Morabito",
                "12/29",
                "123",
                null,
                null,
                null,
                null
        );
    }

    private static DireccionEnvio direccion() {
        return new DireccionEnvio("Av. Corrientes", "1234", "CABA", "Buenos Aires", "C1043");
    }
}
