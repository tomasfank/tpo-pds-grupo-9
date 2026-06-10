package com.riva.model.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;

class CarritoTest {

    @Test
    void agregarItemCreatesItemAndCalcularTotalSumsSubtotal() {
        ProductVariant variante = variante("var-1", BigDecimal.valueOf(120), 5);
        Carrito carrito = new Carrito("cliente-1");

        carrito.agregarItem(variante, 2);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().getFirst().getVariante()).isEqualTo(variante);
        assertThat(carrito.getItems().getFirst().getCantidad()).isEqualTo(2);
        assertThat(carrito.calcularTotal()).isEqualTo(240);
    }

    @Test
    void agregarItemWithSameVarianteAccumulatesCantidadWithoutDuplicatingItem() {
        ProductVariant variante = variante("var-1", BigDecimal.valueOf(75), 5);
        Carrito carrito = new Carrito("cliente-1");

        carrito.agregarItem(variante, 1);
        carrito.agregarItem(variante, 3);

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().getFirst().getCantidad()).isEqualTo(4);
        assertThat(carrito.calcularTotal()).isEqualTo(300);
    }

    @Test
    void modificarCantidadUpdatesExistingItem() {
        ProductVariant variante = variante("var-1", BigDecimal.valueOf(50), 5);
        Carrito carrito = new Carrito("cliente-1");
        carrito.agregarItem(variante, 1);
        ItemCarrito item = carrito.getItems().getFirst();

        carrito.modificarCantidad(item, 4);

        assertThat(item.getCantidad()).isEqualTo(4);
        assertThat(item.subtotal()).isEqualTo(200);
    }

    @Test
    void eliminarItemRemovesOnlySelectedItem() {
        ProductVariant varianteA = variante("var-1", BigDecimal.valueOf(50), 5);
        ProductVariant varianteB = variante("var-2", BigDecimal.valueOf(80), 5);
        Carrito carrito = new Carrito("cliente-1");
        carrito.agregarItem(varianteA, 1);
        carrito.agregarItem(varianteB, 2);

        carrito.eliminarItem(carrito.getItems().getFirst());

        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().getFirst().getVariante()).isEqualTo(varianteB);
        assertThat(carrito.calcularTotal()).isEqualTo(160);
    }

    @Test
    void vaciarLeavesNoItemsAndTotalZero() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.agregarItem(variante("var-1", BigDecimal.valueOf(40), 5), 2);

        carrito.vaciar();

        assertThat(carrito.getItems()).isEmpty();
        assertThat(carrito.calcularTotal()).isZero();
    }

    @Test
    void agregarItemRejectsQuantityGreaterThanStock() {
        ProductVariant variante = variante("var-1", BigDecimal.valueOf(40), 2);
        Carrito carrito = new Carrito("cliente-1");

        assertThatThrownBy(() -> carrito.agregarItem(variante, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock insuficiente");
    }

    private static ProductVariant variante(String id, BigDecimal precioUnitario, int stock) {
        return new ProductVariant(id, Size.M, "Negro", stock, precioUnitario, "prod-1", "Remera");
    }
}
