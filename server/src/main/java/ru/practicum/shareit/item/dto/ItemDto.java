package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.List;

@With
@Value
@Builder
public class ItemDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingItemDto lastBooking;
    BookingItemDto nextBooking;
    List<CommentDto> comments;
    Long requestId;
}
