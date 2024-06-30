package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id")
                                     @Positive(message = "User's id should be positive")
                                     Long userId,
                                 @RequestBody @Valid CreateItemRequestDto creationDto) {
        LocalDateTime creationDate = LocalDateTime.now();
        return itemRequestService.create(userId, creationDate, creationDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader("X-Sharer-User-Id")
                                      @Positive(message = "User's id should be positive")
                                      Long userId,
                                  @PathVariable
                                  @Positive(message = "Request's id should be positive")
                                  Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getByOwner(@RequestHeader("X-Sharer-User-Id")
                                              @Positive(message = "User's id should be positive")
                                              Long userId) {
        return itemRequestService.getByOwnerId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id")
                                           @Positive(message = "User's id should be positive")
                                           Long userId,
                                       @RequestParam(defaultValue = "0")
                                       @PositiveOrZero(message = "Parameter 'from' should be positive or zero")
                                       int from,
                                       @RequestParam(defaultValue = "10")
                                           @Positive(message = "Parameter 'size' should be positive")
                                           int size) {
        return itemRequestService.getAll(userId, from, size);
    }
}
