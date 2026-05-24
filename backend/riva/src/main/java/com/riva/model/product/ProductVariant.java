package com.riva.model.product;

import java.util.Objects;

/**
 * Value object embebido en Product. Representa una combinación talla + color con su stock propio
 * (RIVA.md §2.2 y glosario "Variante").
 *
 * Tanto size como color son nullable para soportar productos sin alguna de las dos dimensiones
 * (accesorios sin talla, prendas de talle único, etc.). La validación de "al menos una dimensión
 * definida" se hace en ProductService al crear el producto, no acá, para mantener la clase como
 * un value object simple.
 */
public class ProductVariant {

    private Size size;
    private String color;
    private int stock;

    protected ProductVariant() {
        // requerido por Spring Data
    }

    public ProductVariant(Size size, String color, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock no puede ser negativo");
        }
        this.size = size;
        this.color = color;
        this.stock = stock;
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
        return Objects.equals(size, other.size) && Objects.equals(color, other.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, color);
    }
}
