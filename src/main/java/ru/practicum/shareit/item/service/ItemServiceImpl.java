package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper mapper;

    private void checkUserId(Long userId) {
        if (!userStorage.existsById(userId)) {
            log.error("Запрос на действие с предметом от несуществующего пользователя с id {}.", userId);
            throw new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        }
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        checkUserId(userId);
        Item item = mapper.toItem(userId, itemDto);
        return mapper.toDto(itemStorage.save(item));
    }

    @Override
    public ItemDto update(Long itemId, Long userId, Map<String, Object> fields) {
        Item item = itemStorage.findItemByOwnerId(userId, itemId).orElseThrow(() -> {
            log.error("Запрос пользователя с id {} на обновления предмета с id {}.", userId, itemId);
            return new NoAccessException("You haven't access to update this item.");
        });
        fields.remove("id");
        fields.forEach((k, v) -> {
            Field field = ReflectionUtils.findField(Item.class, k);
            field.setAccessible(true);
            ReflectionUtils.setField(field, item, v);
        });
        return mapper.toDto(itemStorage.save(item));
    }

    @Override
    public Long delete(Long userId, Long itemId) {
        Optional<Item> optionalItem = itemStorage.findById(itemId);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            if (item.getOwnerId().equals(userId)) {
                itemStorage.deleteById(itemId);
            } else {
                log.error("Запрос пользователя с id {} на удаление предмета с id {}.", userId, itemId);
                throw new NoAccessException("You haven't access to delete this item.");
            }
        }
        return itemId;
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemStorage.findById(id).orElseThrow(() -> {
            log.error("Запрос получить несуществующий предмет с id {}.", id);
            return new NotFoundException(
                    String.format("Item with id %d is not exist.", id)
            );
        });
        return mapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByUser(Long userId) {
        checkUserId(userId);
        return itemStorage.findAllByOwnerId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.search(text).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
