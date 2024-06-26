package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl  implements BookingService {
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingMapper mapper;

    @Transactional(readOnly = true)
    private User getUser(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> {
            log.error("NoAccess. Запрос при бронировании от несуществующего пользователя с id {}.", userId);
            return new NoAccessException("You haven't access to booking. Please, log in.");
        });
    }

    private void throwBadSearchRequest(Long userId, String state) {
        log.error("UnsupportedStatus. Поиск бронирования по несуществующему параметру {}, " +
                "от пользователя с id {}.", state, userId);
        throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
    }

    @Override
    public BookingDto create(Long bookerId, CreateBookingDto requestDto) {
        log.info("Запрос на создание бронирования от пользователя с id {}", bookerId);

        if (!requestDto.getStart().isBefore(requestDto.getEnd())) {
            log.error("BadRequest. Дата начала бронирования позже окончания бронирования с id {}.", bookerId);
            throw new BadRequestException("The booking start date must be before the end date.");
        }
        User booker = getUser(bookerId);
        Item item = itemStorage.findById(requestDto.getItemId()).orElseThrow(() -> {
            log.error("NotFound. Запрос забронировать несуществующий предмет с id {}.", requestDto.getItemId());
            return new NotFoundException(
                    String.format("Item with id %d is not exist.", requestDto.getItemId())
            );
        });
        if (bookerId.equals(item.getOwnerId())) {
            log.error("NotFound. Запрос владельца с id {} забронировать предмет c id {}.",
                    bookerId, requestDto.getItemId());
            throw new NotFoundException("Item cannot be reserved.");
        } else if (item.getAvailable()) {
            Booking booking = bookingStorage.save(mapper.toBooking(requestDto, booker, item));
            return mapper.toDto(booking);
        } else {
            log.error("BadRequest. Запрос забронировать предмет (id {}) со статусом unavailable.", requestDto.getItemId());
            throw new BadRequestException(
                    String.format("Item with id %d is not available.", requestDto.getItemId())
            );
        }
    }

    @Override
    public BookingDto updateStatus(Long ownerId, Long bookingId, Boolean status) {
        log.info("Запрос на изменение статуса бронирования с id {} от пользователя с id {}", bookingId, ownerId);

        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> {
            log.error("NotFound. Запрос обновить статус несуществующего бронирования с id {}.", bookingId);
            return new NotFoundException(
                    String.format("Booking with id %d is not exist.", bookingId)
            );
        });
        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            log.error("NotFound. Запрос обновить статус бронирования (id {}) не от владельца (id {}) предмета",
                    bookingId, ownerId);
            throw new NotFoundException("You haven't access to update this booking.");
        } else if (status && booking.getStatus().equals(BookingStatus.WAITING)) {
            booking.setStatus(BookingStatus.APPROVED);
            bookingStorage.save(booking);
        } else if (!status && booking.getStatus().equals(BookingStatus.WAITING)) {
            booking.setStatus(BookingStatus.REJECTED);
            bookingStorage.save(booking);
        } else {
            log.error("BadRequest. Запрос повторно обновить статус бронирования с id {}.", bookingId);
            throw new BadRequestException("The status of this booking has already been changed");
        }
        return mapper.toDto(booking);
    }

    @Override
    public Long delete(Long ownerId, Long bookingId) {
        log.info("Запрос на удаление бронирования с id {} от пользователя с id {}", bookingId, ownerId);

        Optional<Booking> optionalBooking = bookingStorage.findById(bookingId);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            if (booking.getBooker().getId().equals(ownerId)) {
                bookingStorage.deleteById(bookingId);
            } else {
                log.error("NoAccess. Запрос пользователя с id {} на удаление бронирования с id {}.", ownerId, bookingId);
                throw new NoAccessException("You haven't access to delete this booking.");
            }
        }
        return bookingId;
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto findById(Long userId, Long bookingId) {
        log.info("Запрос на просмотр бронирования с id {} от пользователя с id {}", bookingId, userId);

        Booking booking = bookingStorage.findById(bookingId).orElseThrow(() -> {
            log.error("Запрос получить несуществующее бронирование с id {}.", bookingId);
            return new NotFoundException(
                    String.format("Booking with id %d is not exist.", bookingId)
            );
        });
        if ((booking.getBooker().getId().equals(userId))
                || (booking.getItem().getOwnerId().equals(userId))) {
            return mapper.toDto(booking);
        } else {
            log.error("Не найдено. Запрос пользователя с id {} на просмотр бронирования с id {}.", userId, bookingId);
            throw new NotFoundException("This booking isn't found.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> findAllByBooker(Long bookerId,
                                            String state,
                                            LocalDateTime currentTime,
                                            Pageable pageable) {
        User booker = getUser(bookerId);
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case "CURRENT":
                log.info("Запрос от бронирующего с id {} получить список текущих бронирований.", bookerId);
                bookings = bookingStorage.findCurrentByBookerId(bookerId, currentTime, pageable);
                break;
            case "FUTURE":
                log.info("Запрос от бронирующего с id {} получить список будущих бронирований.", bookerId);
                bookings = bookingStorage.findByBookerIdAndStartDateIsAfter(bookerId, currentTime, pageable);
                break;
            case "WAITING":
                log.info("Запрос от бронирующего с id {} получить список бронирований, ожидающих подтверждения.",
                        bookerId);
                bookings = bookingStorage.findByBookerIdAndStatusIs(bookerId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                log.info("Запрос от бронирующего с id {} получить список отклонённых бронирований.", bookerId);
                bookings = bookingStorage.findByBookerIdAndStatusIs(bookerId, BookingStatus.REJECTED, pageable);
                break;
            case "PAST":
                log.info("Запрос от бронирующего с id {} получить список завершённых бронирований.", bookerId);
                Set<BookingStatus> statuses = Set.of(BookingStatus.WAITING, BookingStatus.REJECTED);
                bookings = bookingStorage.findByBookerIdAndEndDateIsBeforeAndStatusNotIn(bookerId,
                        currentTime, pageable, statuses);
                break;
            case "ALL":
                log.info("Запрос от бронирующего с id {} получить список всех бронирований.", bookerId);
                bookings = bookingStorage.findByBookerId(bookerId, pageable);
                break;
            default:
                throwBadSearchRequest(bookerId, state);
        }
        return bookings.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> findAllByOwnerItems(Long ownerId,
                                                String state,
                                                LocalDateTime currentTime,
                                                Pageable pageable) {
        User owner = getUser(ownerId);
        if (owner.getItems().isEmpty()) {
            return new ArrayList<>();
        }
        List<Booking> bookings = new ArrayList<>();
        switch (state) {
            case "CURRENT":
                log.info("Запрос от владельца вещей с id {} получить список текущих бронирований.", ownerId);
                bookings = bookingStorage.findCurrentByOwner(ownerId, currentTime, pageable);
                break;
            case "FUTURE":
                log.info("Запрос от владельца вещей с id {} получить список будущих бронирований.", ownerId);
                bookings = bookingStorage.findFutureByOwner(ownerId, currentTime, pageable);
                break;
            case "WAITING":
                log.info("Запрос от владельца вещей с id {} получить список бронирований, ожидающих подтверждения.",
                        ownerId);
                bookings = bookingStorage.findByOwnerByStatus(ownerId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                log.info("Запрос от владельца вещей с id {} получить список отклонённых бронирований.", ownerId);
                bookings = bookingStorage.findByOwnerByStatus(ownerId, BookingStatus.REJECTED, pageable);
                break;
            case "PAST":
                log.info("Запрос от владельца вещей с id {} получить список завершённых бронирований.", ownerId);
                bookings = bookingStorage.findPastByOwner(ownerId, currentTime, pageable);
                break;
            case "ALL":
                log.info("Запрос от владельца вещей с id {} получить список всех бронирований.", ownerId);
                bookings = bookingStorage.findAllByOwner(ownerId, pageable);
                break;
            default:
                throwBadSearchRequest(ownerId, state);
        }
        return bookings.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
