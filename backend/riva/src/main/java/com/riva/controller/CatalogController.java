package com.riva.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.CategoryTreeNode;
import com.riva.dto.ProductResponse;
import com.riva.service.CatalogService;

// CU-07 — navegación pública del catálogo. Acceso sin sesión (RIVA §7.4: usuarios no registrados
// también pueden navegar).
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/tree")
    public List<CategoryTreeNode> getCategoryTree() {
        return catalogService.getTree();
    }

    @GetMapping("/categories/{id}/products")
    public List<ProductResponse> getProductsInCategory(@PathVariable String id) {
        return catalogService.findProductsInSubtree(id).stream().map(ProductResponse::from).toList();
    }
}
