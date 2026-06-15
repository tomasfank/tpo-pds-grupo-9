package com.riva.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.riva.dto.CreateProductRequest;
import com.riva.dto.ProductVariantDto;
import com.riva.model.category.Category;
import com.riva.model.product.Size;
import com.riva.model.user.Administrador;
import com.riva.model.user.Cliente;
import com.riva.repository.CategoryRepository;
import com.riva.repository.ProductRepository;
import com.riva.repository.UsuarioRepository;
import com.riva.service.CategoryService;
import com.riva.service.ProductService;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final String WOMAN_TSHIRT_IMAGE_URL = "https://fakestoreapi.com/img/51eg55uWmdL._AC_UX679_t.png";
    private static final String WOMAN_PANTS_IMAGE_URL = "https://fakestoreapi.com/img/71z3kpMAYsL._AC_UY879_t.png";
    private static final String WOMAN_JACKET_IMAGE_URL = "https://fakestoreapi.com/img/51Y5NI-I5jL._AC_UX679_t.png";
    private static final String MAN_TSHIRT_IMAGE_URL = "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_t.png";
    private static final String MAN_PANTS_IMAGE_URL = "https://fakestoreapi.com/img/71YXzeOuslL._AC_UY879_t.png";
    private static final String MAN_JACKET_IMAGE_URL = "https://fakestoreapi.com/img/71li-ujtlUL._AC_UX679_t.png";
    private static final String KIDS_TSHIRT_IMAGE_URL = "https://fakestoreapi.com/img/61pHAEJ4NML._AC_UX679_t.png";

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository,
                      CategoryService categoryService, ProductService productService,
                      UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.productService = productService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Siembra de usuarios (idempotente por email)
        if (usuarioRepository.findByEmail("admin@riva.com").isEmpty()) {
            usuarioRepository.save(new Administrador(
                    "Admin", "RIVA", "admin@riva.com", passwordEncoder.encode("admin12345")));
        }
        if (usuarioRepository.findByEmail("cliente@riva.com").isEmpty()) {
            usuarioRepository.save(new Cliente(
                    "Cliente", "Demo", "cliente@riva.com", passwordEncoder.encode("cliente12345")));
        }

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
                WOMAN_TSHIRT_IMAGE_URL,
                variants(new ProductVariantDto(Size.S, "Blanco", 8), new ProductVariantDto(Size.M, "Negro", 5)));
        product("Pantalon Sastrero Mujer", "Pantalon recto para uso urbano y oficina.",
                "Viscosa sastrera", BigDecimal.valueOf(96), pantalonesMujer.getId(),
                WOMAN_PANTS_IMAGE_URL,
                variants(new ProductVariantDto(Size.M, "Negro", 4), new ProductVariantDto(Size.L, "Gris", 2)));
        product("Campera Liviana Mujer", "Campera corta repelente al agua para media estacion.",
                "Nylon tecnico", BigDecimal.valueOf(148), camperasMujer.getId(),
                WOMAN_JACKET_IMAGE_URL,
                variants(new ProductVariantDto(Size.M, "Arena", 3), new ProductVariantDto(Size.L, "Negro", 0)));

        product("Remera Boxy Hombre", "Remera boxy fit de jersey pesado.",
                "Jersey de algodon", BigDecimal.valueOf(45), remerasHombre.getId(),
                MAN_TSHIRT_IMAGE_URL,
                variants(new ProductVariantDto(Size.M, "Crudo", 6), new ProductVariantDto(Size.L, "Negro", 7)));
        product("Pantalon Cargo Hombre", "Cargo urbano con bolsillos laterales y cintura regulable.",
                "Gabardina", BigDecimal.valueOf(108), pantalonesHombre.getId(),
                MAN_PANTS_IMAGE_URL,
                variants(new ProductVariantDto(Size.L, "Verde", 4), new ProductVariantDto(Size.XL, "Negro", 3)));
        product("Campera Utility Hombre", "Campera funcional con multiples bolsillos.",
                "Ripstop", BigDecimal.valueOf(162), camperasHombre.getId(),
                MAN_JACKET_IMAGE_URL,
                variants(new ProductVariantDto(Size.L, "Azul", 2), new ProductVariantDto(Size.XL, "Negro", 1)));

        product("Remera Mini RIVA", "Remera infantil suave para uso diario.",
                "Algodon", BigDecimal.valueOf(34), remerasNinos.getId(),
                KIDS_TSHIRT_IMAGE_URL,
                variants(new ProductVariantDto(Size.XS, "Blanco", 5), new ProductVariantDto(Size.S, "Rojo", 4)));
    }

    private void product(String name, String description, String material, BigDecimal price, String categoryId,
                         String imageUrl, List<ProductVariantDto> variants) {
        productService.create(new CreateProductRequest(
                name,
                description,
                price,
                material,
                categoryId,
                List.of(imageUrl),
                variants
        ));
    }

    private List<ProductVariantDto> variants(ProductVariantDto... variants) {
        return List.of(variants);
    }
}
