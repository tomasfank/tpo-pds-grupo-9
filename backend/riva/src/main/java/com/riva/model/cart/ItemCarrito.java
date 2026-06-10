package com.riva.model.cart;

import java.math.BigDecimal;
import java.util.UUID;

import com.riva.model.product.ProductVariant;

public class ItemCarrito {

    private String id;

    // UML: ItemCarrito --> Variante.
    // En el codigo actual la clase de variante del catalogo se llama ProductVariant.
    // TODO(uml): renombrar o adaptar a Variante si el modelo de catalogo se alinea 1:1 al UML.
    private ProductVariant variante;
    private int cantidad;

    protected ItemCarrito() {
        // requerido por Spring Data
    }

    public ItemCarrito(ProductVariant variante, int cantidad) {
        this(UUID.randomUUID().toString(), variante, cantidad);
    }

    public ItemCarrito(String id, ProductVariant variante, int cantidad) {
        if (variante == null) {
            throw new IllegalArgumentException("variante es obligatoria");
        }
        validarCantidad(cantidad);
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.variante = variante;
        this.cantidad = cantidad;
    }

    public String getId() {
        return id;
    }

    public ProductVariant getVariante() {
        return variante;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void modificarCantidad(int cantidad) {
        validarCantidad(cantidad);
        this.cantidad = cantidad;
    }

    public double subtotal() {
        return subtotalDecimal().doubleValue();
    }

    public BigDecimal subtotalDecimal() {
        BigDecimal precio = variante.getPrecioUnitario();
        if (precio == null) {
            throw new IllegalStateException("La variante del item no tiene precio unitario");
        }
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad debe ser mayor a cero");
        }
    }
}
