package com.riva.model.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.riva.model.product.Product;
import com.riva.pattern.composite.CatalogComponent;

/**
 * Patrón Composite — Composite.
 *
 * Category puede contener otras Category o Product (hojas) — sus hijos son CatalogComponent.
 * obtenerProductos() recursa sobre los hijos y agrega sus productos; eso es lo que justifica el Composite
 * acá vs un simple "Category con List&lt;Product&gt;": el cliente trata uniformemente nodos
 * intermedios y hojas.
 *
 * Persistencia: solo parentId + ancestorIds se guardan en Mongo. La lista de children es
 * @Transient y la rehidrata CategoryService cuando arma el árbol en memoria (combinando
 * categorías + productos del subárbol). El acceso desde fuera es de solo lectura para evitar
 * que se mute la estructura por accidente; el armado se hace via agregar() dentro del servicio.
 *
 * ancestorIds: lista ordenada raíz → padre (no incluye al propio nodo). Permite:
 *   - Detectar ciclos en O(1) al reubicar (si target.id ∈ subjectAncestorIds → ciclo).
 *   - Query directo "todas las categorías bajo X" con findByAncestorIdsContaining(X).
 */
@Document(collection = "categories")
public class Category implements CatalogComponent {

    @Id
    private String id;
    private String name;
    private boolean active = true;

    @Indexed
    private String parentId;

    @Indexed
    private List<String> ancestorIds = new ArrayList<>();

    @Transient
    private final List<CatalogComponent> children = new ArrayList<>();

    protected Category() {
        // requerido por Spring Data
    }

    public Category(String name, String parentId, List<String> ancestorIds) {
        this.name = name;
        this.parentId = parentId;
        this.ancestorIds = new ArrayList<>(ancestorIds);
        this.active = true;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void reparent(String newParentId, List<String> newAncestorIds) {
        this.parentId = newParentId;
        this.ancestorIds = new ArrayList<>(newAncestorIds);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void agregar(CatalogComponent component) {
        children.add(component);
    }

    public void quitar(CatalogComponent component) {
        children.remove(component);
    }

    public List<CatalogComponent> getChildren() {
        return Collections.unmodifiableList(children);
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
        return false;
    }

    /**
     * Obtiene recursivamente los productos activos del subarbol. Es el caso canonico del
     * Composite: el cliente invoca esta operacion sobre cualquier nodo y no necesita saber si
     * es hoja o compuesto.
     */
    @Override
    public List<Product> obtenerProductos() {
        if (!active) {
            return List.of();
        }
        return children.stream()
                .flatMap(child -> child.obtenerProductos().stream())
                .toList();
    }

    public String getParentId() {
        return parentId;
    }

    public List<String> getAncestorIds() {
        return Collections.unmodifiableList(ancestorIds);
    }
}
