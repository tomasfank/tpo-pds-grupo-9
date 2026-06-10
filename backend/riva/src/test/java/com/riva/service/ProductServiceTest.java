package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.riva.dto.ProductSearchCriteria;
import com.riva.exception.NotFoundException;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    @Test
    void searchActiveProductsFiltersByNameCategorySizeAndColor() {
        Product matching = product(
                "prod-1",
                "Campera urbana",
                "cat-camperas",
                List.of("cat-mujer", "cat-camperas"),
                BigDecimal.valueOf(180),
                List.of(new ProductVariant(Size.M, "Negro", 4))
        );
        Product otherName = product(
                "prod-2",
                "Pantalon recto",
                "cat-pantalones",
                List.of("cat-mujer", "cat-pantalones"),
                BigDecimal.valueOf(110),
                List.of(new ProductVariant(Size.M, "Negro", 3))
        );
        Product otherVariant = product(
                "prod-3",
                "Campera liviana",
                "cat-camperas",
                List.of("cat-mujer", "cat-camperas"),
                BigDecimal.valueOf(150),
                List.of(new ProductVariant(Size.L, "Azul", 2))
        );

        when(productRepository.findByActiveTrue()).thenReturn(List.of(matching, otherName, otherVariant));

        List<Product> result = productService.searchActive(new ProductSearchCriteria(
                "campera",
                "cat-mujer",
                Size.M,
                "negro",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200)
        ));

        assertThat(result).containsExactly(matching);
    }

    @Test
    void requireActiveProductRejectsInactiveProducts() {
        Product inactive = product(
                "prod-1",
                "Remera",
                "cat-remeras",
                List.of("cat-hombre", "cat-remeras"),
                BigDecimal.valueOf(70),
                List.of(new ProductVariant(Size.S, "Blanco", 0))
        );
        inactive.deactivate();

        when(productRepository.findById("prod-1")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> productService.requireActiveProduct("prod-1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    private static Product product(String id, String name, String categoryId, List<String> ancestors,
                                   BigDecimal price, List<ProductVariant> variants) {
        Product product = new Product(
                name,
                "Descripcion",
                price,
                "Algodon",
                categoryId,
                ancestors,
                variants,
                List.of("https://placehold.co/800x1000?text=RIVA")
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
