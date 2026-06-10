package com.riva.model.product;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object embebido en Product. Representa una combinación talla + color con su stock propio
 * (RIVA.md §2.2 y glosario "Variante").
 *
 * Tanto size como color son nullable para soportar productos sin alguna de las dos dimensiones
 * (accesorios sin talla, prendas de talle único, etc.). La validación de "al menos una dimensión
 * definida" se hace en ProductService al crear el producto, no acá, para mantener la clase como
 * un value object simple.
 */
// UML: esta clase cumple el rol de Variante.
// TODO(uml): si se busca equivalencia literal de nombres, renombrar ProductVariant a Variante
// y Size a Talla, manteniendo talla/color/stock como responsabilidades principales.
public class ProductVariant {

    private String id;
    private Size size;
    private String color;
    private int stock;
    private BigDecimal precioUnitario;
    private String productId;
    private String productName;

    protected ProductVariant() {
        // requerido por Spring Data
    }

    public ProductVariant(Size size, String color, int stock) {
        this(UUID.randomUUID().toString(), size, color, stock);
    }

    public ProductVariant(String id, Size size, String color, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock no puede ser negativo");
        }
        this.id = id == null || id.isBlank() ? UUID.randomUUID().toString() : id;
        this.size = size;
        this.color = color;
        this.stock = stock;
    }

    public ProductVariant(String id, Size size, String color, int stock,
                          BigDecimal precioUnitario, String productId, String productName) {
        this(id, size, color, stock);
        this.precioUnitario = precioUnitario;
        this.productId = productId;
        this.productName = productName;
    }

    public String getId() {
        if (id == null || id.isBlank()) {
            id = stableIdFromAttributes();
        }
        return id;
    }

    public Size getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public int getStock() {
        return stock;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public ProductVariant withProductSnapshot(BigDecimal precioUnitario, String productId, String productName) {
        return new ProductVariant(getId(), size, color, stock, precioUnitario, productId, productName);
    }

    private String stableIdFromAttributes() {
        String normalizedSize = size == null ? "unico" : size.name().toLowerCase();
        String normalizedColor = color == null || color.isBlank()
                ? "sin-color"
                : color.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        return normalizedSize + "-" + normalizedColor;
    }

    public boolean matches(Size otherSize, String otherColor) {
        return Objects.equals(size, otherSize) && Objects.equals(color, otherColor);
    }

    public void decreaseStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount debe ser positivo");
        }
        if (amount > stock) {
            throw new IllegalStateException("stock insuficiente para la variante");
        }
        this.stock -= amount;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock no puede ser negativo");
        }
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
