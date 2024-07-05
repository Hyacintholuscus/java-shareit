package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Value
@Builder
@With
public class CreateBookingDto {
    LocalDateTime start;
    LocalDateTime end;
    Long itemId;
}
