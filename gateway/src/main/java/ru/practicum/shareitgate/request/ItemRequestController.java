package ru.practicum.shareitgate.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareitgate.request.dto.CreateItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id")
                                             @Positive(message = "User's id should be positive")
                                             Long userId,
                                         @RequestBody @Valid CreateItemRequestDto creationDto) {
        log.info("Запрос создания запроса от пользователя с id {}", userId);
        return requestClient.createRequest(userId, creationDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id")
                                  @Positive(message = "User's id should be positive")
                                  Long userId,
                                  @PathVariable
                                  @Positive(message = "Request's id should be positive")
                                  Long requestId) {
        log.info("Запрос получения запроса с id {} от пользователя с id {}", requestId, userId);
        return requestClient.getById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(@RequestHeader("X-Sharer-User-Id")
                                           @Positive(message = "User's id should be positive")
                                           Long userId) {
        log.info("Запрос получения запросов пользователя с id {}", userId);
        return requestClient.getByOwner(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id")
                                       @Positive(message = "User's id should be positive")
                                       Long userId,
                                       @RequestParam(defaultValue = "0")
                                       @PositiveOrZero(message = "Parameter 'from' should be positive or zero")
                                       int from,
                                       @RequestParam(defaultValue = "10")
                                       @Positive(message = "Parameter 'size' should be positive")
                                       int size) {
        log.info("Запрос получения всех запросов от пльзователя с id {}", userId);
        return requestClient.getAll(userId, from, size);
    }
}
