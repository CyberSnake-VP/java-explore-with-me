package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.dto.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public CategoryDto create(NewCategoryDto category) {
        log.info("Create new category: {}", category);

        if (categoryRepository.existsByName(category.getName())) {
            throw getValidationException(category.getName());
        } else {
            Category entity = CategoryMapper.mapToEntity(category);
            log.info("Created category: {}", entity);
            return CategoryMapper.mapToDto(categoryRepository.save(entity));
        }
    }

    @Transactional
    @Override
    public CategoryDto update(CategoryDto category, Long id) {
        log.info("Update category: {}", category);
        /** Проверим есть ли такая сущность в бд, после проверим имя на уникальность, иначе выбрасываем кастомные методы исключений*/
        Category entity = validateExistence(id);
        if (entity != null) {
            if (!categoryRepository.existsByName(category.getName())) {
                // изменяем поле name у сущности из бд и снова записываем ее, обновляя.
                entity.setName(category.getName());
                return CategoryMapper.mapToDto(categoryRepository.save(entity));
            } else {
                throw getValidationException(category.getName());
            }
        } else {
            throw getNotFoundException(id);
        }
    }

    @Transactional
    @Override
    public CategoryDto delete(Long id) {
        log.info("Delete category: {}", id);
        Category entity = validateExistence(id);
        if (entity != null) {
            log.info("Category: {} is deleted", entity);
            categoryRepository.delete(entity);
        } else {
            throw getNotFoundException(id);
        }
        return CategoryMapper.mapToDto(entity);
    }

    @Override
    public CategoryDto get(Long id) {
        log.info("Get category: {}", id);
        Category entity = validateExistence(id);
        if (entity != null) {
            log.info("Category: {} is found", entity);
            return CategoryMapper.mapToDto(entity);
        } else {
            throw getNotFoundException(id);
        }
    }

    @Override
    public List<CategoryDto> getAll(Pageable pageable) {
        log.info("Get all categories: {}", pageable);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper::mapToDto)
                .toList();
    }

    private Category validateExistence(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * Для удобства написал два метода, которые выбрасывают исключения.
     */
    private NotFoundException getNotFoundException(Long id) {
        log.info("Category not found with id: {}", id);
        String reason = "The required object was not found.";
        String message = String.format("Category with id=%d was not found", id);
        return new NotFoundException(message, reason);
    }

    private ValidationException getValidationException(String name) {
        log.info("Category already exists with name: {}", name);
        String message = "Category already exists with name: " + name;
        return new ValidationException(message);
    }
}
