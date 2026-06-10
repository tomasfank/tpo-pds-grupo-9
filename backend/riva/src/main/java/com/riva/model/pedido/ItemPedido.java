package com.riva.model.pedido;

import java.math.BigDecimal;
import java.util.UUID;

import com.riva.model.product.ProductVariant;

public class ItemPedido {

    private String id;
    private int cantidad;
    private BigDecimal precioUnitario;
    private String varianteId;
    private String productoId;
    private String productoNombre;
    private String talla;
    private String color;

    protected ItemPedido() {
        // requerido por Spring Data
    }

    public ItemPedido(ProductVariant variante, int cantidad, BigDecimal precioUnitario) {
        this(UUID.randomUUID().toString(), variante, cantidad, precioUnitario);
    }

    public ItemPedido(String id, ProductVariant variante, int cantidad, BigDecimal precioUnitario) {
        if (variante == null) {
            throw new IllegalArgumentException("variante es obligatoria");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad debe ser mayor a cero");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("precioUnitario no puede ser negativo");
        }
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.varianteId = variante.getId();
        this.productoId = variante.getProductId();
        this.productoNombre = variante.getProductName();
        this.talla = variante.getSize() == null ? null : variante.getSize().name();
        this.color = variante.getColor();
    }

    public static ItemPedido desdeVariante(ProductVariant variante, int cantidad) {
        return new ItemPedido(variante, cantidad, variante.getPrecioUnitario());
    }

    public double subtotal() {
        return subtotalDecimal().doubleValue();
    }

    public BigDecimal subtotalDecimal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public String getId() {
        return id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public String getVarianteId() {
        return varianteId;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public String getTalla() {
        return talla;
    }

    public String getColor() {
        return color;
    }
}
