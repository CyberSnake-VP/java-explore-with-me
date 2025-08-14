package ru.practicum.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ErrorResponse {
    private String error;
}
