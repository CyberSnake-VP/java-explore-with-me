package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

public interface CategoryService {
    CategoryDto create(NewCategoryDto category);
    CategoryDto update(CategoryDto category, Long id);
    CategoryDto delete(Long id);
}
