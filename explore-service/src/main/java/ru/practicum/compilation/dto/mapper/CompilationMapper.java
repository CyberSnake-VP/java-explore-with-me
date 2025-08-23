package ru.practicum.compilation.dto.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static CompilationDto mapToCompilationDto(Compilation compilation, List<EventShortDto> eventsShort) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(eventsShort)
                .build();
    }

    public static Compilation mapToEntity(NewCompilationDto newCompilationDto, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .title(newCompilationDto.getTitle())
                .pinned(newCompilationDto.getPinned() != null && newCompilationDto.getPinned())
                .build();
    }

}
