package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.riva.exception.NotFoundException;
import com.riva.model.cart.Carrito;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.repository.CarritoRepository;
import com.riva.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CarritoService carritoService;

    @Test
    void agregarItemCreatesCartWhenClienteDoesNotHaveOneAndStoresSelectedVariante() {
        Product product = product("prod-1", true, variant("var-1", 5));
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito carrito = carritoService.agregarItem("cliente-1", "prod-1", "var-1", 2);

        assertThat(carrito.getClienteId()).isEqualTo("cliente-1");
        assertThat(carrito.getItems()).hasSize(1);
        assertThat(carrito.getItems().getFirst().getVariante().getId()).isEqualTo("var-1");
        assertThat(carrito.getItems().getFirst().getCantidad()).isEqualTo(2);
        verify(carritoRepository).save(carrito);
    }

    @Test
    void modificarCantidadRejectsItemFromAnotherCart() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.agregarItem(new ProductVariant("var-1", Size.M, "Negro", 5, BigDecimal.valueOf(100), "prod-1", "Remera"), 1);
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> carritoService.modificarCantidad("cliente-1", "item-ajeno", 2))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item de carrito no encontrado");
    }

    @Test
    void vaciarClearsClienteCartAndPersistsIt() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.agregarItem(new ProductVariant("var-1", Size.M, "Negro", 5, BigDecimal.valueOf(100), "prod-1", "Remera"), 1);
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito result = carritoService.vaciar("cliente-1");

        assertThat(result.getItems()).isEmpty();
        assertThat(result.calcularTotal()).isZero();
        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).isEmpty();
    }

    @Test
    void productosInactivosDetectaProductosDesactivadosEnElCarrito() {
        Carrito carrito = new Carrito("cliente-1");
        carrito.agregarItem(
                new ProductVariant("var-1", Size.M, "Negro", 5, BigDecimal.valueOf(100), "prod-activo", "Remera"), 1);
        carrito.agregarItem(
                new ProductVariant("var-2", Size.L, "Azul", 5, BigDecimal.valueOf(100), "prod-inactivo", "Campera"), 1);
        Product activo = product("prod-activo", true, variant("var-1", 5));
        Product inactivo = product("prod-inactivo", false, variant("var-2", 5));
        when(productRepository.findAllById(any())).thenReturn(List.of(activo, inactivo));

        Set<String> inactivos = carritoService.productosInactivos(carrito);

        assertThat(inactivos).containsExactly("prod-inactivo");
    }

    @Test
    void productosInactivosVacioCuandoElCarritoEstaVacio() {
        assertThat(carritoService.productosInactivos(new Carrito("cliente-1"))).isEmpty();
    }

    private static Product product(String id, boolean active, ProductVariant... variants) {
        Product product = new Product(
                "Remera",
                "Descripcion",
                BigDecimal.valueOf(100),
                "Algodon",
                "cat-1",
                List.of("cat-1"),
                List.of(variants),
                List.of("https://placehold.co/800x1000?text=RIVA")
        );
        ReflectionTestUtils.setField(product, "id", id);
        if (!active) {
            product.deactivate();
        }
        return product;
    }

    private static ProductVariant variant(String id, int stock) {
        return new ProductVariant(id, Size.M, "Negro", stock);
    }
}
