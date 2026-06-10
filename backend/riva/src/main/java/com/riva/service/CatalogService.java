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
 * CU-07 - Navegar Catalogo, y CU-09 - Ver Detalle.
 *
 * Aca es donde el Composite se materializa: traemos todas las categorias + productos, las
 * vinculamos como hijos de su padre directo (parentId), y exponemos el arbol al cliente.
 * Las operaciones uniformes del Composite se invocan sobre los nodos del arbol sin que el
 * cliente diferencie entre Category y Product.
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
     * Devuelve el arbol completo de categorias con los productos activos como hojas.
     * Ensambla el Composite en memoria y luego lo proyecta al DTO de respuesta.
     */
    public List<CategoryTreeNode> getTree() {
        return buildCatalogTree().stream()
                .filter(c -> c.getParentId() == null)
                .map(this::toTreeNode)
                .toList();
    }

    /**
     * Lista los productos activos contenidos directa o transitivamente bajo una categoria
     * usando la operacion polimorfica obtenerProductos() del Composite.
     */
    public List<Product> findProductsInSubtree(String categoryId) {
        return buildCatalogTree().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .map(Category::obtenerProductos)
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada: " + categoryId));
    }

    private List<Category> buildCatalogTree() {
        List<Category> categories = categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .toList();
        List<Product> activeProducts = productRepository.findByActiveTrue();

        Map<String, Category> byId = new HashMap<>();
        categories.forEach(c -> byId.put(c.getId(), c));

        for (Category c : categories) {
            if (c.getParentId() != null) {
                Category parent = byId.get(c.getParentId());
                if (parent != null) {
                    parent.agregar(c);
                }
            }
        }

        for (Product p : activeProducts) {
            Category category = byId.get(p.getCategoryId());
            if (category != null) {
                category.agregar(p);
            }
        }

        return categories;
    }

    private CategoryTreeNode toTreeNode(Category category) {
        List<CategoryTreeNode> childCategories = category.getChildren().stream()
                .filter(c -> !c.isLeaf())
                .map(Category.class::cast)
                .map(this::toTreeNode)
                .toList();
        int activeProducts = ((CatalogComponent) category).obtenerProductos().size();
        return new CategoryTreeNode(
                category.getId(),
                category.getName(),
                category.isActive(),
                activeProducts,
                childCategories
        );
    }
}
