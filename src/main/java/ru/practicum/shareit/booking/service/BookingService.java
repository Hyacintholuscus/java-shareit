package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingDto create(Long bookerId, CreateBookingDto requestDto);

    BookingDto updateStatus(Long ownerId, Long bookingId, Boolean status);

    Long delete(Long ownerId, Long bookingId);

    BookingDto findById(Long userId, Long bookingId);

    List<BookingDto> findAllByBooker(Long bookerId,
                                     String state,
                                     LocalDateTime currentTime,
                                     Pageable pageable);

    List<BookingDto> findAllByOwnerItems(Long ownerId,
                                         String state,
                                         LocalDateTime currentTime,
                                         Pageable pageable);
}
