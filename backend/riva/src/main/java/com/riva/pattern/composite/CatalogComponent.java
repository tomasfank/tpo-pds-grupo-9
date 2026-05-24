package com.riva.pattern.composite;

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
 * tipo de nodo. countActiveProducts() es el ejemplo canónico: el Product hoja devuelve 0 o 1,
 * y la Category recursa sobre sus hijos.
 *
 * Persistencia: la estructura padre-hijo se materializa en MongoDB con parentId + ancestorIds;
 * la rehidratación al árbol en memoria la hace CategoryService.
 */
public interface CatalogComponent {

    String getId();

    String getName();

    boolean isActive();

    boolean isLeaf();

    int countActiveProducts();
}
