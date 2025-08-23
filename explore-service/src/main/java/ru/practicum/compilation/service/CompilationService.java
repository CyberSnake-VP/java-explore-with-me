package ru.practicum.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilationByAdmin(final NewCompilationDto compilationDto);

    void deleteCompilationByAdmin(final Long compId);

    CompilationDto updateCompilationByAdmin(UpdateCompilationRequest requestDto, Long compId);

    CompilationDto get(Long compId);

    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);
}
