package com.riva.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.riva.dto.PedidoResponse;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.cart.Carrito;
import com.riva.model.pedido.DireccionEnvio;
import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.pattern.payment.MetodoPago;
import com.riva.pattern.payment.MetodoPagoFactory;
import com.riva.pattern.payment.ResultadoPago;
import com.riva.repository.CarritoRepository;
import com.riva.repository.PedidoRepository;
import com.riva.repository.ProductRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductRepository productRepository;
    private final MetodoPagoFactory metodoPagoFactory;

    public PedidoService(PedidoRepository pedidoRepository, CarritoRepository carritoRepository,
                         ProductRepository productRepository, MetodoPagoFactory metodoPagoFactory) {
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.productRepository = productRepository;
        this.metodoPagoFactory = metodoPagoFactory;
    }

    public Pedido crearDesdeCarrito(String clienteId, DireccionEnvio direccionEnvio) {
        validarCliente(clienteId);
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ValidationException("El carrito esta vacio"));
        if (carrito.estaVacio()) {
            throw new ValidationException("El carrito esta vacio");
        }
        carrito.validarStockDisponible();
        List<ItemPedido> items = carrito.getItems().stream()
                .map(item -> ItemPedido.desdeVariante(item.getVariante(), item.getCantidad()))
                .toList();
        Pedido pedido = new Pedido(clienteId, items, direccionEnvio);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarDelCliente(String clienteId) {
        validarCliente(clienteId);
        return pedidoRepository.findByClienteIdOrderByFechaDesc(clienteId).stream()
                .peek(Pedido::reconstruirEstadoDesdePersistencia)
                .toList();
    }

    public Pedido obtenerDetalle(String clienteId, String pedidoId) {
        validarCliente(clienteId);
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado: " + pedidoId));
        if (!clienteId.equals(pedido.getClienteId())) {
            throw new NotFoundException("Pedido no encontrado: " + pedidoId);
        }
        pedido.reconstruirEstadoDesdePersistencia();
        return pedido;
    }

    public Pedido avanzarEstado(String pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado: " + pedidoId));
        pedido.reconstruirEstadoDesdePersistencia();
        pedido.avanzarEstado();
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizarDireccionEnvio(String clienteId, String pedidoId, DireccionEnvio direccionEnvio) {
        Pedido pedido = obtenerDetalle(clienteId, pedidoId);
        String estado = pedido.nombreEstadoActual();
        if ("Enviado".equals(estado) || "Entregado".equals(estado)) {
            throw new ValidationException("La direccion no puede modificarse cuando el pedido ya fue enviado");
        }
        pedido.actualizarDireccionEnvio(direccionEnvio);
        return pedidoRepository.save(pedido);
    }

    public ProcesarPagoResponse procesarPago(String clienteId, String pedidoId, ProcesarPagoRequest request) {
        Pedido pedido = obtenerDetalle(clienteId, pedidoId);
        if (!"Pendiente".equals(pedido.nombreEstadoActual())) {
            throw new ValidationException("Solo se pueden pagar pedidos pendientes");
        }

        MetodoPago metodoPago = metodoPagoFactory.crearDesde(request);
        String metodoPagoNombre = metodoPagoFactory.nombreMetodo(request);
        if (!hayStockDisponible(pedido)) {
            return new ProcesarPagoResponse(
                    false,
                    "Stock insuficiente para procesar el pago",
                    PedidoResponse.from(pedido)
            );
        }

        pedido.cambiarMetodoPago(metodoPago);
        ResultadoPago resultadoPago = pedido.pagar();
        if (!resultadoPago.exito()) {
            return new ProcesarPagoResponse(false, resultadoPago.mensaje(), PedidoResponse.from(pedido));
        }

        descontarStock(pedido);
        pedido.registrarMetodoPago(metodoPagoNombre);
        pedido.avanzarEstado();
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        vaciarCarrito(clienteId);
        return new ProcesarPagoResponse(true, resultadoPago.mensaje(), PedidoResponse.from(pedidoGuardado));
    }

    private boolean hayStockDisponible(Pedido pedido) {
        for (ItemPedido item : pedido.getItems()) {
            Product product = requireActiveProduct(item.getProductoId());
            ProductVariant variante = requireVariant(product, item.getVarianteId());
            if (variante.getStock() < item.getCantidad()) {
                return false;
            }
        }
        return true;
    }

    private void descontarStock(Pedido pedido) {
        for (ItemPedido item : pedido.getItems()) {
            Product product = requireActiveProduct(item.getProductoId());
            ProductVariant variante = requireVariant(product, item.getVarianteId());
            variante.decreaseStock(item.getCantidad());
            productRepository.save(product);
        }
    }

    private Product requireActiveProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + productId));
        if (!product.isActive()) {
            throw new NotFoundException("Producto no encontrado: " + productId);
        }
        return product;
    }

    private ProductVariant requireVariant(Product product, String varianteId) {
        return product.getVariants().stream()
                .filter(variante -> variante.getId().equals(varianteId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Variante no encontrada: " + varianteId));
    }

    private void vaciarCarrito(String clienteId) {
        Carrito carrito = carritoRepository.findByClienteId(clienteId)
                .orElseGet(() -> new Carrito(clienteId));
        carrito.vaciar();
        carritoRepository.save(carrito);
    }

    private void validarCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new ValidationException("clienteId es obligatorio");
        }
    }
}
