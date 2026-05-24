package com.riva.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.riva.model.category.Category;

public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(String parentId);

    /** Todas las categorías descendientes (directas o transitivas) de la categoría dada. */
    List<Category> findByAncestorIdsContaining(String ancestorId);

    boolean existsByParentId(String parentId);
}
