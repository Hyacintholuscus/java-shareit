package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final CommentMapper commentMapper;

    public ItemDto toDto(Item item, BookingItemDto lastBooking, BookingItemDto nextBooking) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments((item.getComments() == null) ? new ArrayList<>() :
                        item.getComments().stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments((item.getComments() == null) ? new ArrayList<>() :
                        item.getComments().stream()
                        .map(commentMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public Item toItem(Long ownerId, ItemDto dto) {
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .ownerId(ownerId)
                .build();
    }
}
