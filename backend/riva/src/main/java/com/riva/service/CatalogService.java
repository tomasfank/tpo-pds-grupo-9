package com.riva.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.riva.dto.CategoryTreeNode;
import com.riva.exception.NotFoundException;
import com.riva.model.category.Category;
import com.riva.model.product.Product;
import com.riva.pattern.composite.CatalogComponent;
import com.riva.repository.CategoryRepository;
import com.riva.repository.ProductRepository;

/**
 * CU-07 — Navegar Catálogo, y CU-09 — Ver Detalle.
 *
 * Acá es donde el Composite se "materializa": traemos todas las categorías + productos, las
 * vinculamos como hijos de su padre directo (parentId), y exponemos el árbol al cliente.
 * Las operaciones uniformes del Composite (countActiveProducts) se invocan luego sobre los
 * nodos del árbol sin que el cliente diferencie entre Category y Product.
 */
@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Devuelve el árbol completo de categorías con los productos activos como hojas.
     * Ensambla el Composite en memoria y luego lo proyecta al DTO de respuesta.
     */
    public List<CategoryTreeNode> getTree() {
        List<Category> categories = categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .toList();
        List<Product> activeProducts = productRepository.findByActiveTrue();

        Map<String, Category> byId = new HashMap<>();
        categories.forEach(c -> byId.put(c.getId(), c));

        // Vincular cada categoría con su padre (Composite real en memoria).
        for (Category c : categories) {
            if (c.getParentId() != null) {
                Category parent = byId.get(c.getParentId());
                if (parent != null) {
                    parent.addChild(c);
                }
            }
        }

        // Vincular productos a su categoría directa (siguen siendo hojas del Composite).
        for (Product p : activeProducts) {
            Category cat = byId.get(p.getCategoryId());
            if (cat != null) {
                cat.addChild(p);
            }
        }

        // Devolver solo las raíces; el resto cuelga por children.
        return categories.stream()
                .filter(c -> c.getParentId() == null)
                .map(this::toTreeNode)
                .toList();
    }

    /**
     * Lista los productos activos contenidos directa o transitivamente bajo una categoría
     * (CU-07 §4: "directa o transitivamente").
     *
     * Implementación: query denormalizado sobre categoryAncestorIds + productos directos.
     * Más eficiente que recorrer el árbol y barato gracias a los índices.
     */
    public List<Product> findProductsInSubtree(String categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Categoría no encontrada: " + categoryId);
        }
        List<Product> direct = productRepository.findByCategoryIdAndActiveTrue(categoryId);
        List<Product> nested = productRepository.findByCategoryAncestorIdsContainingAndActiveTrue(categoryId);
        // Merge sin duplicados — un producto directo NO aparece en nested (su ancestorIds no se
        // contiene a sí mismo), pero por las dudas evitamos duplicados.
        Map<String, Product> dedup = new HashMap<>();
        direct.forEach(p -> dedup.put(p.getId(), p));
        nested.forEach(p -> dedup.putIfAbsent(p.getId(), p));
        return List.copyOf(dedup.values());
    }

    private CategoryTreeNode toTreeNode(Category category) {
        List<CategoryTreeNode> childCategories = category.getChildren().stream()
                .filter(c -> !c.isLeaf())
                .map(Category.class::cast)
                .map(this::toTreeNode)
                .toList();
        // countActiveProducts() es del Composite: una sola llamada cuenta todo el subárbol.
        int activeProducts = ((CatalogComponent) category).countActiveProducts();
        return new CategoryTreeNode(
                category.getId(),
                category.getName(),
                category.isActive(),
                activeProducts,
                childCategories
        );
    }
}
