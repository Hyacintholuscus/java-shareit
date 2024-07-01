package ru.practicum.shareitgate.booking.dto;

import lombok.*;
import ru.practicum.shareitgate.item.dto.ItemDto;
import ru.practicum.shareitgate.user.dto.UserDto;

import java.time.LocalDateTime;

@Value
@Builder
public class BookingDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    UserDto booker;
    ItemDto item;
}
