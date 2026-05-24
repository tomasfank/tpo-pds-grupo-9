package com.riva.dto;

// parentId puede ser null para mover la categoría a la raíz del árbol.
public record MoveCategoryRequest(String parentId) {
}
