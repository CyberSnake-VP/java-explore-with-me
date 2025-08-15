package ru.practicum.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody final NewCategoryDto category) {
        log.info("POST /admin/categories/ Create category: {}", category);
        return categoryService.create(category);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CategoryDto delete(@PathVariable(name = "catId") Long catId) {
        log.info("DELETE /admin/categories/ Delete category with id: {}", catId);
        return categoryService.delete(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto update(@Valid @RequestBody final CategoryDto category,
                              @PathVariable(name = "catId") Long catId) {
        log.info("PATCH /admin/categories/ Update category: {}, catId: {}", category, catId);
        return categoryService.update(category, catId);
    }

    @GetMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto get(@PathVariable(name = "catId") Long catId) {
        log.info("GET /admin/categories/ Get category with id: {}", catId);
        return categoryService.get(catId);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getAll(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                    @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /admin/categories");
        return categoryService.getAll(PageRequest.of(from, size));
    }
}
