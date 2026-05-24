package com.riva.dto;

import java.util.List;

import com.riva.model.category.Category;

public record CategoryResponse(
        String id,
        String name,
        boolean active,
        String parentId,
        List<String> ancestorIds
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isActive(),
                category.getParentId(),
                category.getAncestorIds()
        );
    }
}
