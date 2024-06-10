package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.validation.IsAfter;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@With
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@IsAfter(minDate = "start", maxDate = "end", message = "Start date must be before end date.")
public class BookingDto {
    private Long id;
    @Future
    private LocalDateTime start;
    @Future
    private LocalDateTime end;
    @NotBlank
    private BookingStatus status;
    private UserDto booker;
    private ItemDto item;
}
