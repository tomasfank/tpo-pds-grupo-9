package com.riva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.riva.dto.CategoryTreeNode;
import com.riva.model.category.Category;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.model.product.Size;
import com.riva.repository.CategoryRepository;
import com.riva.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void getTreeReturnsOnlyActiveCategoriesAndCountsActiveProductsFromSubtree() {
        Category women = category("cat-women", "Mujer", null, List.of(), true);
        Category shirts = category("cat-shirts", "Remeras", "cat-women", List.of("cat-women"), true);
        Category hidden = category("cat-hidden", "Oculta", "cat-women", List.of("cat-women"), false);
        Product product = product("prod-shirt", "Remera basica", "cat-shirts", List.of("cat-women", "cat-shirts"), true);

        when(categoryRepository.findAll()).thenReturn(List.of(women, shirts, hidden));
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product));

        List<CategoryTreeNode> tree = catalogService.getTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).id()).isEqualTo("cat-women");
        assertThat(tree.get(0).activeProducts()).isEqualTo(1);
        assertThat(tree.get(0).children())
                .extracting(CategoryTreeNode::id)
                .containsExactly("cat-shirts");
    }

    private static Category category(String id, String name, String parentId, List<String> ancestors, boolean active) {
        Category category = new Category(name, parentId, ancestors);
        ReflectionTestUtils.setField(category, "id", id);
        if (!active) {
            category.deactivate();
        }
        return category;
    }

    private static Product product(String id, String name, String categoryId, List<String> ancestors, boolean active) {
        Product product = new Product(
                name,
                "Descripcion",
                BigDecimal.valueOf(120),
                "Algodon",
                categoryId,
                ancestors,
                List.of(new ProductVariant(Size.M, "Negro", 5)),
                List.of("https://placehold.co/800x1000?text=RIVA")
        );
        ReflectionTestUtils.setField(product, "id", id);
        if (!active) {
            product.deactivate();
        }
        return product;
    }
}
