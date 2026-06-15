package com.riva.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.riva.dto.AgregarItemCarritoRequest;
import com.riva.dto.CarritoResponse;
import com.riva.dto.ModificarCantidadCarritoRequest;
import com.riva.model.cart.Carrito;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.model.user.Rol;
import com.riva.security.UsuarioPrincipal;
import com.riva.service.CarritoService;

class CarritoControllerTest {

    private final CarritoService carritoService = mock(CarritoService.class);
    private final CarritoController controller = new CarritoController(carritoService);

    @Test
    void getReturnsClienteCartFromPrincipal() {
        Carrito carrito = carritoConItem();
        when(carritoService.obtenerCarrito("cliente-1")).thenReturn(carrito);

        CarritoResponse response = controller.get(principal());

        assertThat(response.clienteId()).isEqualTo("cliente-1");
        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo("200");
    }

    @Test
    void postDelegatesAgregarItemAndReturnsUpdatedCart() {
        Carrito carrito = carritoConItem();
        when(carritoService.agregarItem("cliente-1", "prod-1", "var-1", 2)).thenReturn(carrito);

        CarritoResponse response = controller.agregarItem(
                principal(),
                new AgregarItemCarritoRequest("prod-1", "var-1", 2)
        );

        verify(carritoService).agregarItem("cliente-1", "prod-1", "var-1", 2);
        assertThat(response.items().getFirst().variantId()).isEqualTo("var-1");
    }

    @Test
    void patchDelegatesModificarCantidadAndReturnsUpdatedCart() {
        Carrito carrito = carritoConItem();
        when(carritoService.modificarCantidad("cliente-1", "item-1", 3)).thenReturn(carrito);

        controller.modificarCantidad(principal(), "item-1", new ModificarCantidadCarritoRequest(3));

        verify(carritoService).modificarCantidad("cliente-1", "item-1", 3);
    }

    @Test
    void deleteItemDelegatesEliminarItemAndReturnsUpdatedCart() {
        Carrito carrito = new Carrito("cliente-1");
        when(carritoService.eliminarItem("cliente-1", "item-1")).thenReturn(carrito);

        CarritoResponse response = controller.eliminarItem(principal(), "item-1");

        verify(carritoService).eliminarItem("cliente-1", "item-1");
        assertThat(response.items()).isEmpty();
    }

    @Test
    void deleteItemsDelegatesVaciarAndReturnsEmptyCart() {
        Carrito carrito = new Carrito("cliente-1");
        when(carritoService.vaciar("cliente-1")).thenReturn(carrito);

        CarritoResponse response = controller.vaciar(principal());

        verify(carritoService).vaciar("cliente-1");
        assertThat(response.total()).isEqualByComparingTo("0");
    }

    private static UsuarioPrincipal principal() {
        return new UsuarioPrincipal("cliente-1", "cliente-1@riva.com", "hash", Rol.CLIENTE);
    }

    private static Carrito carritoConItem() {
        Carrito carrito = new Carrito("cliente-1");
        ProductVariant variante = new ProductVariant(
                "var-1",
                Size.M,
                "Negro",
                5,
                BigDecimal.valueOf(100),
                "prod-1",
                "Remera"
        );
        carrito.agregarItem(variante, 2);
        return carrito;
    }
}
