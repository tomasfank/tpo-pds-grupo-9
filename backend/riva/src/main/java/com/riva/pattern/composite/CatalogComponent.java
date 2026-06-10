package com.riva.pattern.composite;

import java.util.List;

import com.riva.model.product.Product;

/**
 * Patrón Composite — Component.
 *
 * Define las operaciones comunes que pueden invocarse sobre cualquier nodo del catálogo,
 * sin importar si es una hoja (Product) o un compuesto (Category). Permite que el cliente
 * (CatalogService, controllers) recorra el árbol uniformemente.
 *
 * Motivación del patrón en este punto: RF-07 + CU-07 + CU-13. El catálogo es una jerarquía
 * en la que las categorías contienen subcategorías o productos. Composite evita el "if leaf
 * else recurse" en el código cliente y deja la recursión encapsulada en la operación de cada
 * tipo de nodo. obtenerProductos() es la operacion del UML: el Product hoja se devuelve a si
 * mismo y la Category recursa sobre sus hijos.
 *
 * Persistencia: la estructura padre-hijo se materializa en MongoDB con parentId + ancestorIds;
 * la rehidratación al árbol en memoria la hace CategoryService.
 */
public interface CatalogComponent {

    String getId();

    String getName();

    boolean isActive();

    boolean isLeaf();

    List<Product> obtenerProductos();
}
