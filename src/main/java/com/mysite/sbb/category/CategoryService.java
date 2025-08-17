package com.mysite.sbb.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getTopCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public List<Category> getChildren(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Category saveCategory(String name, Long parentId) {
        Category parent = parentId != null ? categoryRepository.findById(parentId).orElse(null) : null;
        Category category = Category.builder().name(name).parent(parent).build();
        return categoryRepository.save(category);
    }
}
