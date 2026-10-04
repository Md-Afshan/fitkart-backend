package com.fitkart.service.impl;

import com.fitkart.exception.ResourceNotFoundException;
import com.fitkart.dto.category.CategoryRequest;
import com.fitkart.dto.category.CategoryResponse;
import com.fitkart.entity.Category;
import com.fitkart.exception.ResourceAlreadyExistsException;
import com.fitkart.repository.CategoryRepository;
import com.fitkart.repository.ProductRepository;
import com.fitkart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Category name is already exists."
            );
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        LocalDateTime now = LocalDateTime.now();
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id
                        )
                );

        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {

            throw new ResourceAlreadyExistsException(
                    "Category name is already exists."
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found with id: " + id
                        )
                );

        if (productRepository.existsByCategory_Id(id)) {
            throw new ResourceAlreadyExistsException(
                    "Category cannot be deleted because it contains products."
            );
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        return response;
    }
}