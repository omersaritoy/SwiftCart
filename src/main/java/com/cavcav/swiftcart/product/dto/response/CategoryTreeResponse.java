package com.cavcav.swiftcart.product.dto.response;

import com.cavcav.swiftcart.product.model.Category;

import java.util.*;

public record CategoryTreeResponse(
        String id,
        String name,
        String description,
        List<CategoryTreeResponse> subCategories
) {

    public static List<CategoryTreeResponse> buildTree(List<Category> allCategories) {
        Map<String, CategoryTreeResponse> map = new HashMap<>();
        allCategories.forEach(category ->
                map.put(category.getId(), new CategoryTreeResponse(category.getId(), category.getName(), category.getDescription(), new ArrayList<>())));
        List<CategoryTreeResponse> result = new ArrayList<>();
        allCategories.forEach(category -> {
            CategoryTreeResponse node = map.get(category.getId());
            if (category.getParentCategory() == null)
                result.add(node);
            else {
                CategoryTreeResponse parent = map.get(category.getParentCategory().getId());
                if (parent != null)
                    parent.subCategories.add(node);
            }
        });
        return result;
    }

//    public static CategoryTreeResponse from(Category category, List<Category> allCategories) {
//        List<CategoryTreeResponse> subCategories = allCategories.stream()
//                .filter(c->c.getParentCategory()!=null&&c.getParentCategory().getId().equals(category.getId()))
//                .map(c->CategoryTreeResponse.from(c,allCategories)).toList();
//
//        return new CategoryTreeResponse(category.getId(), category.getName(), category.getDescription(), subCategories);
//    }
}
