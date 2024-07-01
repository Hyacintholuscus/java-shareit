package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
                                    @RequestHeader("X-Sharer-User-Id") Long authorId,
                                    @RequestBody CreateCommentDto commentRequestDto) {
        LocalDateTime createdTime = LocalDateTime.now();
        return itemService.createComment(itemId, authorId, createdTime, commentRequestDto);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody Map<String, Object> fields) {
        LocalDateTime currentTime = LocalDateTime.now();
        return itemService.update(itemId, userId, fields, currentTime);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long userId) {
        LocalDateTime currentTime = LocalDateTime.now();
        return itemService.getById(userId, itemId, currentTime);
    }

    @GetMapping
    public List<ItemDto> getItemsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam int from,
                                        @RequestParam int size) {
        LocalDateTime currentTime = LocalDateTime.now();
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        return itemService.getAllByUser(userId, currentTime, pageable);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestParam String text,
                                     @RequestParam int from,
                                     @RequestParam int size) {
        Pageable pageable = PageRequest.of(from, size);
        return itemService.search(userId, text, pageable);
    }

    @DeleteMapping("/{itemId}")
    public Long deleteItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long itemId) {
        return itemService.delete(userId, itemId);
    }
}
