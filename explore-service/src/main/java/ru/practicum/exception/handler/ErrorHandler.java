package ru.practicum.exception.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleValidate(final ValidationException e) {
        log.warn(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleError(final RuntimeException e) {
        log.warn(e.getMessage(), e);
        return  ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Internal Server Error")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.warn(e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .reason(e.getReason())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ApiError> handeConstraintViolation(final ConstraintViolationException e) {
        final List<Violation> violations = e.getConstraintViolations().stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .toList();

        List<ApiError> apiErrors = new ArrayList<>();

        for (Violation violation : violations) {
            apiErrors.add(ApiError.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(violation.getMessage())
                    .reason("Incorrectly made request.")
                    .timestamp(LocalDateTime.now())
                    .build());
        }

        return apiErrors;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ApiError> handleMethodArgumentNotValid(final MethodArgumentNotValidException e) {
       final List<Violation> violations = e.getBindingResult().getFieldErrors().stream()
               .map(error-> new Violation(error.getField(), error.getDefaultMessage()))
               .toList();

       List<ApiError> apiErrors = new ArrayList<>();

       for (Violation violation : violations) {
           apiErrors.add(ApiError.builder()
                   .status(HttpStatus.BAD_REQUEST)
                   .message(violation.getMessage())
                   .reason("Incorrectly made request.")
                   .timestamp(LocalDateTime.now())
                   .build());
       }

       return apiErrors;
    }


}
