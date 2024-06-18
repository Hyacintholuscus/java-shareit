package ru.practicum.shareit.booking.dto;

import lombok.*;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BookingRequestDto {
    @NotNull(message = "Start time shouldn't be null.")
    @Future(message = "Start time should be in future.")
    private LocalDateTime start;
    @NotNull(message = "End time shouldn't be null.")
    @Future(message = "End time should be in future.")
    private LocalDateTime end;
    @NotNull(message = "Item's id shouldn't be null.")
    @Positive(message = "End time should be positive.")
    private Long itemId;
}
