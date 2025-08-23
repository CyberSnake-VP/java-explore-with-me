package ru.practicum.exception.handler;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.DateValidationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleError(final RuntimeException e) {
        log.warn("Error: {}", e.getMessage());
        return ErrorResponse.builder().error(e.getMessage()).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Ошибка при валидации: {}", ex.getMessage());
        return ErrorResponse.builder().error(ex.getMessage()).build();
    }

    // В отличие от проверки тела запроса,
    // при нарушении ограничений на уровне параметров выбрасывается исключение ConstraintViolationException
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleConstraintViolation(final ConstraintViolationException e) {
        log.info("Constraint violation: {}", e.getMessage());
        log.warn(e.getMessage());
        return ErrorResponse.builder().error(e.getMessage()).build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorResponse handleDateValidation(final DateValidationException e) {
        log.error("Ошибка при валидации дат: {}", e.getMessage());
        return ErrorResponse.builder().error(e.getMessage()).build();
    }

}
