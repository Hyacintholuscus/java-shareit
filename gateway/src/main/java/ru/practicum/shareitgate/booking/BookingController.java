package ru.practicum.shareitgate.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareitgate.booking.dto.BookingState;
import ru.practicum.shareitgate.booking.dto.CreateBookingDto;
import ru.practicum.shareitgate.exception.BadRequestException;
import ru.practicum.shareitgate.exception.UnsupportedStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id")
                                                    @Positive(message = "User's id should be positive")
                                                    Long bookerId,
                                                @RequestBody @Valid CreateBookingDto creationDto) {
        if (!creationDto.getStart().isBefore(creationDto.getEnd())) {
            log.error("BadRequest. Дата начала бронирования позже окончания бронирования с id {}.", bookerId);
            throw new BadRequestException("The booking start date must be before the end date.");
        }
        log.info("Запрос создания бронирования предмета с id {} от пользователя с id {}",
                creationDto.getItemId(), bookerId);
        return bookingClient.createBooking(bookerId, creationDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateStatus(@RequestHeader("X-Sharer-User-Id")
                                   @Positive(message = "User's id should be positive")
                                   Long ownerId,
                                   @PathVariable
                                   @Positive(message = "Booking's id should be positive")
                                   Long bookingId,
                                   @RequestParam Boolean approved) {
        log.info("Запрос обновления статуса бронирования с id {} от пользователя с id {}", bookingId, ownerId);
        return bookingClient.updateStatus(ownerId, bookingId, approved);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Object> delete(@RequestHeader("X-Sharer-User-Id")
                                             @Positive(message = "User's id should be positive")
                                             Long bookerId,
                                         @PathVariable
                                         @Positive(message = "Booking's id should be positive")
                                         Long bookingId) {
        log.info("Запрос удаления бронирования с id {} от пользователя с id {}", bookingId, bookerId);
        return bookingClient.delete(bookerId, bookingId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id")
                               @Positive(message = "User's id should be positive")
                               Long userId,
                               @PathVariable
                               @Positive(message = "Booking's id should be positive")
                               Long bookingId) {
        log.info("Запрос получения бронирования с id {} от пользователя с id {}", bookingId, userId);
        return bookingClient.findById(userId, bookingId);
    }

    private UnsupportedStatusException throwUnsupportedStatus(Long userId, String state) {
        log.error("UnsupportedStatus. Поиск бронирования по несуществующему параметру {}, " +
                "от пользователя с id {}.", state, userId);
        throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
    }

    @GetMapping
    public ResponseEntity<Object> findAllByBooker(@RequestHeader("X-Sharer-User-Id")
                                            @Positive(message = "User's id should be positive")
                                            Long bookerId,
                                            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                            @RequestParam(defaultValue = "0")
                                            @PositiveOrZero(message = "Parameter 'from' should be positive or zero")
                                            int from,
                                            @RequestParam(defaultValue = "10")
                                            @Positive(message = "Parameter 'size' should be positive")
                                            int size) {
        BookingState state = BookingState.getState(stateParam).orElseThrow(
            () -> throwUnsupportedStatus(bookerId, stateParam)
        );
        log.info("Запрос получения всех бронирований пользователя с id {}", bookerId);
        return bookingClient.findAllByBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findAllByOwner(@RequestHeader("X-Sharer-User-Id")
                                           @Positive(message = "User's id should be positive")
                                           Long ownerId,
                                           @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                           @RequestParam(defaultValue = "0")
                                           @PositiveOrZero(message = "Parameter 'from' should be positive or zero")
                                           int from,
                                           @RequestParam(defaultValue = "10")
                                           @Positive(message = "Parameter 'size' should be positive")
                                           int size) {
        BookingState state = BookingState.getState(stateParam).orElseThrow(
                () -> throwUnsupportedStatus(ownerId, stateParam)
        );
        log.info("Запрос получения всех бронирований владельца предметов с id {}", ownerId);
        return bookingClient.findAllByOwner(ownerId, state, from, size);
    }
}
