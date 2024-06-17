package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id")
                                        @Positive(message = "User's id should be positive")
                                        Long bookerId,
                                    @RequestBody @Valid BookingRequestDto bookingRequestDto) {
        return bookingService.create(bookerId, bookingRequestDto);
    }

    @PatchMapping("{bookingId}")
    public BookingDto updateStatus(@RequestHeader("X-Sharer-User-Id")
                                       @Positive(message = "User's id should be positive")
                                       Long ownerId,
                                   @PathVariable
                                   @Positive(message = "Booking's id should be positive")
                                   Long bookingId,
                                   @RequestParam Boolean approved) {
        if (approved == null) {
            log.error("BadRequest. Смена статуса бронирования (id {}) " +
                    "на null от пользователя с  id {}", bookingId, ownerId);
            throw new BadRequestException("Parameter approved should be true or false");
        }
        return bookingService.updateStatus(ownerId, bookingId, approved);
    }

    @DeleteMapping("/{bookingId}")
    public Long delete(@RequestHeader("X-Sharer-User-Id")
                           @Positive(message = "User's id should be positive")
                           Long bookerId,
                       @PathVariable
                       @Positive(message = "Booking's id should be positive")
                       Long bookingId) {
        return bookingService.delete(bookerId, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id")
                                   @Positive(message = "User's id should be positive")
                                   Long userId,
                               @PathVariable
                                   @Positive(message = "Booking's id should be positive")
                                   Long bookingId) {
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findAllByBooker(@RequestHeader("X-Sharer-User-Id")
                                                @Positive(message = "User's id should be positive")
                                                Long bookerId,
                                            @RequestParam(defaultValue = "ALL") String state) {
        LocalDateTime currentTime = LocalDateTime.now();
        return bookingService.findAllByBooker(bookerId, state, currentTime);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id")
                                               @Positive(message = "User's id should be positive")
                                               Long bookerId,
                                           @RequestParam(defaultValue = "ALL") String state) {
        LocalDateTime currentTime = LocalDateTime.now();
        return bookingService.findAllByOwnerItems(bookerId, state, currentTime);
    }
}
