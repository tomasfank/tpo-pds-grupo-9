package com.riva.dto;

import java.util.List;

// Representa un nodo del árbol de categorías para la respuesta del navegador del catálogo.
public record CategoryTreeNode(
        String id,
        String name,
        boolean active,
        int activeProducts,
        List<CategoryTreeNode> children
) {
}
