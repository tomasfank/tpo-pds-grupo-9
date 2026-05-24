package com.riva.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.riva.model.product.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByActiveTrue();

    List<Product> findByCategoryId(String categoryId);

    /** Productos directamente bajo la categoría dada (no recursivo). */
    List<Product> findByCategoryIdAndActiveTrue(String categoryId);

    /** Productos en toda la jerarquía bajo la categoría dada (recursivo). */
    List<Product> findByCategoryAncestorIdsContainingAndActiveTrue(String ancestorId);

    boolean existsByCategoryIdAndActiveTrue(String categoryId);

    boolean existsByCategoryAncestorIdsContainingAndActiveTrue(String ancestorId);
}
