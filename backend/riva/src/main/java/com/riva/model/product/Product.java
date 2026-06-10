package com.riva.model.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.riva.pattern.composite.CatalogComponent;

/**
 * Patrón Composite — Leaf.
 *
 * Product es la hoja del árbol del catálogo. obtenerProductos() devuelve este producto si esta
 * activo o una lista vacia si no; no hay recursion porque no tiene hijos. La pertenencia a una categoría
 * se modela con categoryId (única) + categoryAncestorIds (lista de ancestros, denormalizada para
 * permitir queries de "todos los productos bajo la categoría X" sin recorrer recursivamente).
 *
 * Stock vive a nivel variante (talla + color), no a nivel producto, según RIVA.md §2.2.
 */
@Document(collection = "products")
public class Product implements CatalogComponent {

    public static final String DEFAULT_BRAND = "RIVA";

    @Id
    private String id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private String material;
    private List<String> imageUrls = new ArrayList<>();
    private boolean active = true;

    @Indexed
    private String categoryId;

    @Indexed
    private List<String> categoryAncestorIds = new ArrayList<>();

    private List<ProductVariant> variants = new ArrayList<>();

    protected Product() {
        // requerido por Spring Data
    }

    public Product(String name, String description, BigDecimal price, String material,
                   String categoryId, List<String> categoryAncestorIds,
                   List<ProductVariant> variants, List<String> imageUrls) {
        this.name = name;
        this.description = description;
        this.brand = DEFAULT_BRAND;
        this.price = price;
        this.material = material;
        this.categoryId = categoryId;
        this.categoryAncestorIds = new ArrayList<>(categoryAncestorIds);
        this.variants = new ArrayList<>(variants);
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
        this.active = true;
    }

    public void update(String name, String description, BigDecimal price, String material,
                       List<ProductVariant> variants, List<String> imageUrls) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (material != null) this.material = material;
        if (variants != null) this.variants = new ArrayList<>(variants);
        if (imageUrls != null) this.imageUrls = new ArrayList<>(imageUrls);
    }

    public void reassignCategory(String categoryId, List<String> categoryAncestorIds) {
        this.categoryId = categoryId;
        this.categoryAncestorIds = new ArrayList<>(categoryAncestorIds);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<Product> obtenerProductos() {
        return active ? List.of(this) : List.of();
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getMaterial() {
        return material;
    }

    public List<String> getImageUrls() {
        return Collections.unmodifiableList(imageUrls);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public List<String> getCategoryAncestorIds() {
        return Collections.unmodifiableList(categoryAncestorIds);
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }
}
