package com.riva.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.CategoryResponse;
import com.riva.dto.CreateCategoryRequest;
import com.riva.dto.MoveCategoryRequest;
import com.riva.dto.UpdateCategoryRequest;
import com.riva.model.category.Category;
import com.riva.service.CategoryService;

import jakarta.validation.Valid;

// CU-13 — endpoints administrativos para gestionar la jerarquía de categorías.
// TODO(auth): restringir a rol Administrador cuando se implemente CU-03 + JWT.
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> listAll() {
        return categoryService.findAll().stream().map(CategoryResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest req) {
        Category created = categoryService.create(req.name(), req.parentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(created));
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@PathVariable String id, @Valid @RequestBody UpdateCategoryRequest req) {
        return CategoryResponse.from(categoryService.rename(id, req.name()));
    }

    @PutMapping("/{id}/parent")
    public CategoryResponse move(@PathVariable String id, @RequestBody MoveCategoryRequest req) {
        return CategoryResponse.from(categoryService.move(id, req.parentId()));
    }

    @DeleteMapping("/{id}")
    public CategoryResponse deactivate(@PathVariable String id) {
        return CategoryResponse.from(categoryService.deactivate(id));
    }

    @PostMapping("/{id}/activate")
    public CategoryResponse activate(@PathVariable String id) {
        return CategoryResponse.from(categoryService.activate(id));
    }
}
