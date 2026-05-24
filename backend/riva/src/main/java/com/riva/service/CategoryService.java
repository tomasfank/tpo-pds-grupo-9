package com.riva.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.riva.exception.ConflictException;
import com.riva.exception.NotFoundException;
import com.riva.exception.ValidationException;
import com.riva.model.category.Category;
import com.riva.repository.CategoryRepository;
import com.riva.repository.ProductRepository;

/**
 * CU-13 — Gestionar Categorías y Subcategorías.
 *
 * Responsable de mantener la integridad de la jerarquía:
 *   - calcular ancestorIds al crear/reubicar (denormalización para queries eficientes),
 *   - impedir ciclos al reubicar (RF-11 + CU-13 excepciones),
 *   - impedir desactivar categorías que tengan productos activos en el subárbol
 *     (RF-11 + CU-13 alt 6a),
 *   - cuando se reubica un nodo, recalcular ancestorIds de todo su subárbol y propagar
 *     el cambio a categoryAncestorIds de los productos afectados.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public Category create(String name, String parentId) {
        List<String> ancestorIds = computeAncestorIds(parentId);
        Category category = new Category(name, parentId, ancestorIds);
        return categoryRepository.save(category);
    }

    public Category rename(String id, String newName) {
        Category category = requireCategory(id);
        category.rename(newName);
        return categoryRepository.save(category);
    }

    /**
     * Reubica una categoría bajo un nuevo padre (o a la raíz si newParentId es null).
     * Valida ciclos y recalcula ancestorIds de todo el subárbol más los productos afectados.
     */
    public Category move(String id, String newParentId) {
        Category subject = requireCategory(id);

        if (id.equals(newParentId)) {
            throw new ValidationException("Una categoría no puede ser padre de sí misma");
        }

        // Subárbol del nodo a mover: el nuevo padre no puede estar dentro.
        List<Category> subtree = loadSubtreeIncluding(subject);
        if (newParentId != null) {
            boolean targetInSubtree = subtree.stream().anyMatch(c -> c.getId().equals(newParentId));
            if (targetInSubtree) {
                throw new ValidationException("La categoría destino está dentro del subárbol que se intenta mover (ciclo)");
            }
        }

        List<String> newAncestorIds = computeAncestorIds(newParentId);
        subject.reparent(newParentId, newAncestorIds);

        // Recalcular ancestorIds de todos los descendientes manteniendo su prefijo relativo al subject.
        List<String> oldSubjectAncestors = subject.getAncestorIds(); // ya está reparenteado: estos son los nuevos.
        List<Category> updated = new ArrayList<>();
        updated.add(subject);

        for (Category descendant : subtree) {
            if (descendant.getId().equals(subject.getId())) continue;
            List<String> rebuiltAncestors = rebuildDescendantAncestors(descendant, subject, oldSubjectAncestors);
            descendant.reparent(descendant.getParentId(), rebuiltAncestors);
            updated.add(descendant);
        }

        categoryRepository.saveAll(updated);

        // Propagar a productos: cualquier producto bajo subject o sus descendientes ve cambiar
        // su categoryAncestorIds. Lo recalculamos a partir de su categoryId actual.
        updateProductAncestorsInSubtree(subject.getId());

        return subject;
    }

    public Category deactivate(String id) {
        Category category = requireCategory(id);
        if (productRepository.existsByCategoryAncestorIdsContainingAndActiveTrue(id)
                || productRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new ConflictException("No se puede desactivar la categoría: hay productos activos asociados en el subárbol");
        }
        category.deactivate();
        return categoryRepository.save(category);
    }

    public Category activate(String id) {
        Category category = requireCategory(id);
        category.activate();
        return categoryRepository.save(category);
    }

    public Category requireCategory(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + id));
    }

    public List<String> computeAncestorIds(String parentId) {
        if (parentId == null) {
            return List.of();
        }
        Category parent = requireCategory(parentId);
        List<String> chain = new ArrayList<>(parent.getAncestorIds());
        chain.add(parent.getId());
        return chain;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    private List<Category> loadSubtreeIncluding(Category root) {
        List<Category> descendants = new ArrayList<>(categoryRepository.findByAncestorIdsContaining(root.getId()));
        descendants.add(0, root);
        return descendants;
    }

    private List<String> rebuildDescendantAncestors(Category descendant, Category movedSubject,
                                                    List<String> subjectAncestors) {
        // Mantiene el sufijo del descendant relativo al subject y le antepone los ancestros nuevos del subject.
        List<String> oldAncestors = descendant.getAncestorIds();
        int subjectIdx = oldAncestors.indexOf(movedSubject.getId());
        List<String> suffix = (subjectIdx >= 0)
                ? oldAncestors.subList(subjectIdx, oldAncestors.size())
                : List.of(movedSubject.getId());
        List<String> rebuilt = new ArrayList<>(subjectAncestors);
        rebuilt.addAll(suffix);
        return rebuilt;
    }

    private void updateProductAncestorsInSubtree(String movedSubjectId) {
        Map<String, List<String>> ancestorsByCategoryId = new HashMap<>();
        List<Category> affected = new ArrayList<>(categoryRepository.findByAncestorIdsContaining(movedSubjectId));
        Category movedSubject = requireCategory(movedSubjectId);
        affected.add(movedSubject);

        for (Category c : affected) {
            List<String> chain = new ArrayList<>(c.getAncestorIds());
            chain.add(c.getId());
            ancestorsByCategoryId.put(c.getId(), chain);
        }

        affected.forEach(c -> productRepository.findByCategoryId(c.getId()).forEach(p -> {
            p.reassignCategory(p.getCategoryId(), ancestorsByCategoryId.get(p.getCategoryId()));
            productRepository.save(p);
        }));
    }
}
