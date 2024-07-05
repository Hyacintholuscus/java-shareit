package ru.practicum.shareitgate.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Value
@Builder
public class BookingItemDto {
    Long id;
    LocalDateTime start;
    LocalDateTime end;
    BookingStatus status;
    Long bookerId;
}
