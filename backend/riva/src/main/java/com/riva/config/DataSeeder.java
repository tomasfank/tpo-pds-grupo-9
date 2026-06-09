package com.riva.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.riva.dto.CreateProductRequest;
import com.riva.dto.ProductVariantDto;
import com.riva.model.category.Category;
import com.riva.model.product.Size;
import com.riva.repository.CategoryRepository;
import com.riva.repository.ProductRepository;
import com.riva.service.CategoryService;
import com.riva.service.ProductService;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String IMAGE_URL = "https://placehold.co/800x1000?text=RIVA";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductService productService;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository,
                      CategoryService categoryService, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }

        Category mujer = categoryService.create("Mujer", null);
        Category hombre = categoryService.create("Hombre", null);
        Category ninos = categoryService.create("Ninos", null);

        Category remerasMujer = categoryService.create("Remeras", mujer.getId());
        Category pantalonesMujer = categoryService.create("Pantalones", mujer.getId());
        Category camperasMujer = categoryService.create("Camperas", mujer.getId());
        Category remerasHombre = categoryService.create("Remeras", hombre.getId());
        Category pantalonesHombre = categoryService.create("Pantalones", hombre.getId());
        Category camperasHombre = categoryService.create("Camperas", hombre.getId());
        Category remerasNinos = categoryService.create("Remeras", ninos.getId());

        product("Remera Essential Mujer", "Remera de algodon premium con calce relajado.",
                "Algodon pima", BigDecimal.valueOf(42), remerasMujer.getId(),
                variants(new ProductVariantDto(Size.S, "Blanco", 8), new ProductVariantDto(Size.M, "Negro", 5)));
        product("Pantalon Sastrero Mujer", "Pantalon recto para uso urbano y oficina.",
                "Viscosa sastrera", BigDecimal.valueOf(96), pantalonesMujer.getId(),
                variants(new ProductVariantDto(Size.M, "Negro", 4), new ProductVariantDto(Size.L, "Gris", 2)));
        product("Campera Liviana Mujer", "Campera corta repelente al agua para media estacion.",
                "Nylon tecnico", BigDecimal.valueOf(148), camperasMujer.getId(),
                variants(new ProductVariantDto(Size.M, "Arena", 3), new ProductVariantDto(Size.L, "Negro", 0)));

        product("Remera Boxy Hombre", "Remera boxy fit de jersey pesado.",
                "Jersey de algodon", BigDecimal.valueOf(45), remerasHombre.getId(),
                variants(new ProductVariantDto(Size.M, "Crudo", 6), new ProductVariantDto(Size.L, "Negro", 7)));
        product("Pantalon Cargo Hombre", "Cargo urbano con bolsillos laterales y cintura regulable.",
                "Gabardina", BigDecimal.valueOf(108), pantalonesHombre.getId(),
                variants(new ProductVariantDto(Size.L, "Verde", 4), new ProductVariantDto(Size.XL, "Negro", 3)));
        product("Campera Utility Hombre", "Campera funcional con multiples bolsillos.",
                "Ripstop", BigDecimal.valueOf(162), camperasHombre.getId(),
                variants(new ProductVariantDto(Size.L, "Azul", 2), new ProductVariantDto(Size.XL, "Negro", 1)));

        product("Remera Mini RIVA", "Remera infantil suave para uso diario.",
                "Algodon", BigDecimal.valueOf(34), remerasNinos.getId(),
                variants(new ProductVariantDto(Size.XS, "Blanco", 5), new ProductVariantDto(Size.S, "Rojo", 4)));
    }

    private void product(String name, String description, String material, BigDecimal price, String categoryId,
                         List<ProductVariantDto> variants) {
        productService.create(new CreateProductRequest(
                name,
                description,
                price,
                material,
                categoryId,
                List.of(IMAGE_URL),
                variants
        ));
    }

    private List<ProductVariantDto> variants(ProductVariantDto... variants) {
        return List.of(variants);
    }
}
