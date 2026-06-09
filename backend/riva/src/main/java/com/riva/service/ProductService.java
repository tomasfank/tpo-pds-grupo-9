package com.riva.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.riva.dto.CreateProductRequest;
import com.riva.dto.ProductSearchCriteria;
import com.riva.dto.ProductVariantDto;
import com.riva.dto.UpdateProductRequest;
import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.category.Category;
import com.riva.model.product.Product;
import com.riva.model.product.ProductVariant;
import com.riva.repository.ProductRepository;

/**
 * CU-10 / CU-11 / CU-12 — alta, edición y desactivación de productos.
 *
 * Apoya en CategoryService para resolver la categoría y su cadena de ancestros (denormalizada
 * en el producto para hacer eficiente el query "todos los productos bajo categoría X").
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public Product create(CreateProductRequest req) {
        Category category = categoryService.requireCategory(req.categoryId());
        if (!category.isActive()) {
            throw new ValidationException("La categoría destino está inactiva");
        }
        List<ProductVariant> variants = mapVariants(req.variants());
        List<String> ancestorChain = new ArrayList<>(category.getAncestorIds());
        ancestorChain.add(category.getId());

        Product product = new Product(
                req.name(),
                req.description(),
                req.price(),
                req.material(),
                category.getId(),
                ancestorChain,
                variants,
                req.imageUrls()
        );
        return productRepository.save(product);
    }

    public Product update(String id, UpdateProductRequest req) {
        Product product = requireProduct(id);

        List<ProductVariant> newVariants = req.variants() == null ? null : mapVariants(req.variants());
        product.update(req.name(), req.description(), req.price(), req.material(), newVariants, req.imageUrls());

        if (req.categoryId() != null && !req.categoryId().equals(product.getCategoryId())) {
            Category newCategory = categoryService.requireCategory(req.categoryId());
            if (!newCategory.isActive()) {
                throw new ValidationException("La categoría destino está inactiva");
            }
            List<String> ancestorChain = new ArrayList<>(newCategory.getAncestorIds());
            ancestorChain.add(newCategory.getId());
            product.reassignCategory(newCategory.getId(), ancestorChain);
        }

        return productRepository.save(product);
    }

    public Product deactivate(String id) {
        Product product = requireProduct(id);
        product.deactivate();
        return productRepository.save(product);
    }

    public Product requireProduct(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + id));
    }

    public Product requireActiveProduct(String id) {
        Product product = requireProduct(id);
        if (!product.isActive()) {
            throw new NotFoundException("Producto no encontrado: " + id);
        }
        return product;
    }

    public List<Product> findAllActive() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> searchActive(ProductSearchCriteria criteria) {
        return productRepository.findByActiveTrue().stream()
                .filter(product -> matchesName(product, criteria.name()))
                .filter(product -> matchesCategory(product, criteria.categoryId()))
                .filter(product -> matchesSize(product, criteria.size()))
                .filter(product -> matchesColor(product, criteria.color()))
                .filter(product -> matchesPriceMin(product, criteria.priceMin()))
                .filter(product -> matchesPriceMax(product, criteria.priceMax()))
                .toList();
    }

    private List<ProductVariant> mapVariants(List<ProductVariantDto> dtos) {
        // Reglas: al menos una variante (lo asegura @NotEmpty en el DTO) y cada variante debe
        // definir al menos talla o color (no puede ser una "variante vacía"). Además no se
        // permiten variantes duplicadas (misma combinación talla+color).
        List<ProductVariant> variants = dtos.stream()
                .map(ProductVariantDto::toDomain)
                .toList();

        for (ProductVariant v : variants) {
            if (v.getSize() == null && (v.getColor() == null || v.getColor().isBlank())) {
                throw new ValidationException("Cada variante debe definir al menos talla o color");
            }
        }

        long distinct = variants.stream()
                .map(v -> v.getSize() + "|" + (v.getColor() == null ? "" : v.getColor().toLowerCase()))
                .collect(Collectors.toSet()).size();
        if (distinct != variants.size()) {
            throw new ValidationException("Las variantes no pueden repetir la combinación talla+color");
        }
        return variants;
    }

    private boolean matchesName(Product product, String name) {
        if (name == null || name.isBlank()) {
            return true;
        }
        return product.getName() != null
                && product.getName().toLowerCase().contains(name.trim().toLowerCase());
    }

    private boolean matchesCategory(Product product, String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return true;
        }
        String normalized = categoryId.trim();
        return normalized.equals(product.getCategoryId()) || product.getCategoryAncestorIds().contains(normalized);
    }

    private boolean matchesSize(Product product, com.riva.model.product.Size size) {
        if (size == null) {
            return true;
        }
        return product.getVariants().stream().anyMatch(variant -> size.equals(variant.getSize()));
    }

    private boolean matchesColor(Product product, String color) {
        if (color == null || color.isBlank()) {
            return true;
        }
        String normalized = color.trim().toLowerCase();
        return product.getVariants().stream()
                .anyMatch(variant -> variant.getColor() != null
                        && variant.getColor().trim().toLowerCase().equals(normalized));
    }

    private boolean matchesPriceMin(Product product, java.math.BigDecimal priceMin) {
        return priceMin == null || product.getPrice().compareTo(priceMin) >= 0;
    }

    private boolean matchesPriceMax(Product product, java.math.BigDecimal priceMax) {
        return priceMax == null || product.getPrice().compareTo(priceMax) <= 0;
    }
}
