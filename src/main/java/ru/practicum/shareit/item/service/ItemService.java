package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long itemId, Long userId, Map<String, Object> fields);

    Long delete(Long userId, Long itemId);

    ItemDto getById(Long id);

    List<ItemDto> getAllByUser(Long userId);

    List<ItemDto> search(String text);
}
