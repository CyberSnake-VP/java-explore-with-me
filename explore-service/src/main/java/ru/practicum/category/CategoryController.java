package ru.practicum.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody final NewCategoryDto category) {
        log.info("POST /admin/categories/ Create category: {}", category);
        return categoryService.create(category);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CategoryDto delete(@PathVariable(name = "catId") Long catId) {
        log.info("DELETE /admin/categories/ Delete category: {}", catId);
        return categoryService.delete(catId);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@Valid @RequestBody final CategoryDto category,
                              @PathVariable(name = "catId") Long catId) {
        log.info("PATCH /admin/categories/ Update category: {}", category);
        return categoryService.update(category, catId);
    }
}
