package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Long userId, Item item);

    Item update(Item item);

    Long delete(Long userId, Long itemId);

    Item getById(Long id);

    List<Item> getByUserId(Long itemsIds);

    List<Item> search(String text);
}
