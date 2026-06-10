package com.riva.model.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.riva.model.product.ProductVariant;

@Document(collection = "carts")
public class Carrito {

    @Id
    private String id;

    // UML: Cliente "1" --> "1" Carrito : posee.
    // TODO(usuarios): reemplazar este id tecnico por una relacion directa con Cliente
    // cuando exista com.riva.model.user.Cliente.
    @Indexed(unique = true)
    private String clienteId;

    private List<ItemCarrito> items = new ArrayList<>();

    protected Carrito() {
        // requerido por Spring Data
    }

    public Carrito(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("clienteId es obligatorio");
        }
        this.clienteId = clienteId;
    }

    // UML: agregarItem(variante, cantidad). ProductVariant es la Variante actual del catalogo.
    // TODO(uml): si se renombra ProductVariant a Variante, mantener esta firma conceptual.
    public void agregarItem(ProductVariant variante, int cantidad) {
        validarVariante(variante);
        validarCantidad(cantidad);

        Optional<ItemCarrito> existente = buscarItemPorVariante(variante);
        if (existente.isPresent()) {
            int cantidadFinal = existente.get().getCantidad() + cantidad;
            validarStock(variante, cantidadFinal);
            existente.get().modificarCantidad(cantidadFinal);
            return;
        }

        validarStock(variante, cantidad);
        items.add(new ItemCarrito(variante, cantidad));
    }

    public void modificarCantidad(ItemCarrito item, int cantidad) {
        if (item == null || !items.contains(item)) {
            throw new IllegalArgumentException("item no pertenece al carrito");
        }
        validarCantidad(cantidad);
        validarStock(item.getVariante(), cantidad);
        item.modificarCantidad(cantidad);
    }

    public void eliminarItem(ItemCarrito item) {
        if (item == null || !items.remove(item)) {
            throw new IllegalArgumentException("item no pertenece al carrito");
        }
    }

    public void vaciar() {
        items.clear();
    }

    public double calcularTotal() {
        return calcularTotalDecimal().doubleValue();
    }

    public BigDecimal calcularTotalDecimal() {
        return items.stream()
                .map(ItemCarrito::subtotalDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public List<ItemCarrito> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Optional<ItemCarrito> buscarItem(String itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    public void validarStockDisponible() {
        items.forEach(item -> validarStock(item.getVariante(), item.getCantidad()));
    }

    private Optional<ItemCarrito> buscarItemPorVariante(ProductVariant variante) {
        return items.stream()
                .filter(item -> mismaVariante(item.getVariante(), variante))
                .findFirst();
    }

    private boolean mismaVariante(ProductVariant actual, ProductVariant buscada) {
        return actual.getId().equals(buscada.getId())
                && java.util.Objects.equals(actual.getProductId(), buscada.getProductId());
    }

    private void validarVariante(ProductVariant variante) {
        if (variante == null) {
            throw new IllegalArgumentException("variante es obligatoria");
        }
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad debe ser mayor a cero");
        }
    }

    private void validarStock(ProductVariant variante, int cantidad) {
        if (cantidad > variante.getStock()) {
            throw new IllegalArgumentException("stock insuficiente para la variante");
        }
    }
}
