package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    CommentDto createComment(Long itemId, Long authorId, LocalDateTime createdTime, CommentRequestDto commentRequestDto);

    ItemDto update(Long itemId, Long userId, Map<String, Object> fields, LocalDateTime currentTime);

    Long delete(Long userId, Long itemId);

    ItemDto getById(Long userId, Long itemId, LocalDateTime currentTime);

    List<ItemDto> getAllByUser(Long userId, LocalDateTime currentTime, Pageable pageable);

    List<ItemDto> search(String text, Pageable pageable);
}
