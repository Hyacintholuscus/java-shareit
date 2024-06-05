package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(ItemDto itemDto);

    Long delete(Long userId, Long itemId);

    ItemDto getById(Long id);

    List<ItemDto> getAllByUser(Long userId);

    List<ItemDto> search(String text);
}
