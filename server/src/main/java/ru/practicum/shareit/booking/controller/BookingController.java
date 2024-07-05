package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                    @RequestBody CreateBookingDto bookingRequestDto) {
        return bookingService.create(bookerId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        return bookingService.updateStatus(ownerId, bookingId, approved);
    }

    @DeleteMapping("/{bookingId}")
    public Long delete(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                       @PathVariable Long bookingId) {
        return bookingService.delete(bookerId, bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long bookingId) {
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findAllByBooker(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                            @RequestParam String state,
                                            @RequestParam int from,
                                            @RequestParam int size) {
        LocalDateTime currentTime = LocalDateTime.now();
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        return bookingService.findAllByBooker(bookerId, state, currentTime, pageable);
    }

    @GetMapping("/owner")
    public List<BookingDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                           @RequestParam String state,
                                           @RequestParam int from,
                                           @RequestParam int size) {
        LocalDateTime currentTime = LocalDateTime.now();
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        return bookingService.findAllByOwnerItems(ownerId, state, currentTime, pageable);
    }
}
