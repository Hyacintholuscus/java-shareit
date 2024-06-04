package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.user.controller.UserController;

import javax.validation.ValidationException;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = {
        UserController.class,
        ItemController.class})
public class ErrorHandler {
    private void log(Throwable e) {
        log.error("Исключение {}: {}", e, e.getMessage());
    }

    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidate(final ValidationException e) {
        log(e);
        return Map.of("error", "Validation exception",
                "errorMessage", e.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        log(e);
        return Map.of("error", "Object is not found",
                "errorMessage", e.getMessage());
    }

    @ExceptionHandler({NoAccessException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleNoAccess(final NoAccessException e) {
        log(e);
        return Map.of("error", "No access",
                "errorMessage", e.getMessage());
    }

    @ExceptionHandler({DuplicateException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicate(final DuplicateException e) {
        log(e);
        return Map.of("error", "Duplicate exception",
                "errorMessage", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOtherExc(final Exception e) {
        log(e);
        return Map.of("error", "No access",
                "errorMessage", e.getMessage());
    }
}
