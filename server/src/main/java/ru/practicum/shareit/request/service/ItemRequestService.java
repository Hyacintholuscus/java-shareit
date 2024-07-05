package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, LocalDateTime creationDate, CreateItemRequestDto creationDto);

    ItemRequestDto getById(Long userId, Long requestId);

    List<ItemRequestDto> getByOwnerId(Long userId);

    List<ItemRequestDto> getAll(Long userId, int from, int size);
}
