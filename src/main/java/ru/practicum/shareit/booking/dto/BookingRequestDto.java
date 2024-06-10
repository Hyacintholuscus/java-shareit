package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.validation.IsAfter;

import javax.validation.constraints.Future;
import java.time.LocalDate;

@With
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@IsAfter(minDate = "start", maxDate = "end", message = "Start date must be before end date.")
public class BookingRequestDto {
    private Long id;
    @Future
    private LocalDate start;
    @Future
    private LocalDate end;
}
