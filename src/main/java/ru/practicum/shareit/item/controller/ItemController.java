package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id")
                                  @Positive(message = "User's id should be positive")
                                  Long userId,
            @RequestBody @Valid ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@PathVariable
                                  @Positive(message = "Item's id should be positive")
                                  Long itemId,
                              @RequestHeader("X-Sharer-User-Id")
                              @Positive(message = "User's id should be positive") Long userId,
                              @RequestBody Map<String, Object> fields) {
        List<ItemDto> usersItems = itemService.getAllByUser(userId);
        ItemDto itemDto = itemService.getById(itemId);
        fields.remove("id");
        if (usersItems.contains(itemDto)) {
            fields.forEach((k, v) -> {
                Field field = ReflectionUtils.findField(ItemDto.class, k);
                field.setAccessible(true);
                ReflectionUtils.setField(field, itemDto, v);
            });
        } else {
            log.error("Запрос не владельца обновить предмет с id {}.", itemId);
            throw new NoAccessException("You haven't access to update this item.");
        }
        return itemService.update(itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable
                                   @Positive(message = "Item's id should be positive")
                                   Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByUser(@RequestHeader("X-Sharer-User-Id")
                                            @Positive(message = "User's id should be positive")
                                            Long userId) {
        return itemService.getAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        } else return itemService.search(text);
    }

    @DeleteMapping("/{itemId}")
    public Long deleteItem(@RequestHeader("X-Sharer-User-Id")
                               @Positive(message = "User's id should be positive")
                               Long userId,
                           @PathVariable
                           @Positive(message = "Item's id should be positive")
                           Long itemId) {
        return itemService.delete(userId, itemId);
    }
}
