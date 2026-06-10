package com.riva.service;

import org.springframework.stereotype.Service;

import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.cart.Carrito;
import com.riva.model.cart.ItemCarrito;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.repository.CarritoRepository;
import com.riva.repository.ProductRepository;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductRepository productRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductRepository productRepository) {
        this.carritoRepository = carritoRepository;
        this.productRepository = productRepository;
    }

    public Carrito obtenerCarrito(String clienteId) {
        return obtenerOCrearCarrito(clienteId);
    }

    public Carrito agregarItem(String clienteId, String productId, String variantId, int cantidad) {
        Carrito carrito = obtenerOCrearCarrito(clienteId);
        ProductVariant variante = requireVarianteActiva(productId, variantId);
        carrito.agregarItem(variante, cantidad);
        return carritoRepository.save(carrito);
    }

    public Carrito modificarCantidad(String clienteId, String itemId, int cantidad) {
        Carrito carrito = obtenerOCrearCarrito(clienteId);
        ItemCarrito item = carrito.buscarItem(itemId)
                .orElseThrow(() -> new NotFoundException("Item de carrito no encontrado: " + itemId));
        carrito.modificarCantidad(item, cantidad);
        return carritoRepository.save(carrito);
    }

    public Carrito eliminarItem(String clienteId, String itemId) {
        Carrito carrito = obtenerOCrearCarrito(clienteId);
        ItemCarrito item = carrito.buscarItem(itemId)
                .orElseThrow(() -> new NotFoundException("Item de carrito no encontrado: " + itemId));
        carrito.eliminarItem(item);
        return carritoRepository.save(carrito);
    }

    public Carrito vaciar(String clienteId) {
        Carrito carrito = obtenerOCrearCarrito(clienteId);
        carrito.vaciar();
        return carritoRepository.save(carrito);
    }

    private Carrito obtenerOCrearCarrito(String clienteId) {
        // UML: el carrito pertenece a Cliente. Hasta que exista autenticacion/JWT y clase Cliente,
        // la capa HTTP entrega clienteId como identificador temporal del cliente propietario.
        // TODO(usuarios): resolver cliente desde sesion autenticada y asociar Carrito con Cliente real.
        if (clienteId == null || clienteId.isBlank()) {
            throw new ValidationException("clienteId es obligatorio");
        }
        return carritoRepository.findByClienteId(clienteId)
                .orElseGet(() -> new Carrito(clienteId));
    }

    private ProductVariant requireVarianteActiva(String productId, String variantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + productId));
        if (!product.isActive()) {
            throw new NotFoundException("Producto no encontrado: " + productId);
        }
        ProductVariant variante = product.getVariants().stream()
                .filter(candidate -> candidate.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Variante no encontrada: " + variantId));
        return variante.withProductSnapshot(product.getPrice(), product.getId(), product.getName());
    }
}
