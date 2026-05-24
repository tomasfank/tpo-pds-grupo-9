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

import com.riva.dto.CreateProductRequest;
import com.riva.dto.ProductResponse;
import com.riva.dto.UpdateProductRequest;
import com.riva.model.product.Product;
import com.riva.service.ProductService;

import jakarta.validation.Valid;

// CU-09 (detalle público) + CU-10/11/12 (admin).
// TODO(auth): POST/PUT/DELETE deben requerir rol Administrador cuando entre JWT.
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> listAll() {
        return productService.findAllActive().stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable String id) {
        return ProductResponse.from(productService.requireProduct(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        Product created = productService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(created));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable String id, @Valid @RequestBody UpdateProductRequest req) {
        return ProductResponse.from(productService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ProductResponse deactivate(@PathVariable String id) {
        return ProductResponse.from(productService.deactivate(id));
    }
}
