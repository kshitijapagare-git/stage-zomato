package com.stagezomato.ecommerce.service;

import com.stagezomato.ecommerce.dto.CategoryRequest;
import com.stagezomato.ecommerce.entity.Category;
import com.stagezomato.ecommerce.exception.CategoryConflictException;
import com.stagezomato.ecommerce.exception.ResourceNotFoundException;
import com.stagezomato.ecommerce.repository.CategoryRepository;
import com.stagezomato.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Category create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new CategoryConflictException("Category already exists with name: " + request.name());
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Category> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = getById(id);
        if (categoryRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new CategoryConflictException("Category already exists with name: " + request.name());
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = getById(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryConflictException("Cannot delete category with id: " + id + " because it is referenced by existing products");
        }
        categoryRepository.delete(category);
    }
}
