package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper mapper;

    private void checkUserId(Long userId) {
        if (!userStorage.contains(userId)) {
            log.error("Запрос по деуствию с предметом от несуществующего пользователя с id {}.", userId);
            throw new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        }
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        checkUserId(userId);
        Item item = mapper.toItem(itemDto);
        return mapper.toDto(itemStorage.create(userId, item));
    }

    @Override
    public ItemDto update(ItemDto itemDto) {
        Item item = itemStorage.update(mapper.toItem(itemDto));
        return mapper.toDto(item);
    }

    @Override
    public Long delete(Long userId, Long itemId) {
        checkUserId(userId);
        return itemStorage.delete(userId, itemId);
    }

    @Override
    public ItemDto getById(Long id) {
        return mapper.toDto(itemStorage.getById(id));
    }

    @Override
    public List<ItemDto> getAllByUser(Long userId) {
        checkUserId(userId);
        return itemStorage.getByUserId(userId).stream()
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
