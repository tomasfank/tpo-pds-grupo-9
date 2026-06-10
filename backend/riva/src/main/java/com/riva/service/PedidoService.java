package com.riva.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.cart.Carrito;
import com.riva.model.pedido.DireccionEnvio;
import com.riva.model.pedido.ItemPedido;
import com.riva.model.pedido.Pedido;
import com.riva.repository.CarritoRepository;
import com.riva.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;

    public PedidoService(PedidoRepository pedidoRepository, CarritoRepository carritoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
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

    private void validarCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new ValidationException("clienteId es obligatorio");
        }
    }
}
