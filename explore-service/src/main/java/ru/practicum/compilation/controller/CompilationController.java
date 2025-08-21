package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService service;

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createAdmin(@RequestBody @Valid final NewCompilationDto compilationDto) {
        log.info("POST /admin/compilations  compilation: {}", compilationDto);
        return service.addCompilationByAdmin(compilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAdmin(@PathVariable("compId") Long compId) {
        log.info("DELETE compilation id: {}", compId);
        service.deleteCompilationByAdmin(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateAdmin(@RequestBody @Valid final UpdateCompilationRequest updateCompilationRequest,
                                      @PathVariable("compId") Long compId) {
        log.info("PATCH compilation id: {}, with request body: {}", compId, updateCompilationRequest);
        return service.updateCompilationByAdmin(updateCompilationRequest, compId);
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto get(@PathVariable("compId") Long compId) {
        log.info("GET compilation id: {}", compId);
        return service.get(compId);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getAll(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                       @RequestParam(name = "from", defaultValue = "0") Integer from,
                                       @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET all compilations with pinned: {}, from: {}, size: {}", pinned, from, size);
        return service.getAll(pinned, PageRequest.of(from, size));
    }
}
