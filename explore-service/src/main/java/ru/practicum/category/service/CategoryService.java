package ru.practicum.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto category);
    CategoryDto update(CategoryDto category, Long id);
    CategoryDto delete(Long id);
    CategoryDto get(Long id);
    List<CategoryDto> getAll(Pageable pageable);
}
