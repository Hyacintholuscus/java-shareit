package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private final BookingService bookingService;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    private CreateBookingDto creationDto;
    private BookingDto bookingDto;
    private User ownerItem;
    private User booker;
    private Item item;

    @BeforeEach
    void beforeEach() {
        ownerItem = userStorage.save(User.builder()
                .name("user")
                .email("user@email.ru")
                .build());
        booker = userStorage.save(User.builder()
                .name("booker")
                .email("booker@email.ru")
                .build());
        item = itemStorage.save(Item.builder()
                        .name("item")
                        .description("description")
                        .available(true)
                        .ownerId(ownerItem.getId())
                .build());
        creationDto = CreateBookingDto.builder()
                .start(LocalDateTime.now().plusMinutes(10).truncatedTo(ChronoUnit.MICROS))
                .end(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MICROS))
                .itemId(item.getId())
                .build();
    }

    @Test
    public void shouldCreateBooking() {
        bookingDto = bookingService.create(booker.getId(), creationDto);

        assertNotNull(bookingDto);
        assertNotNull(bookingDto.getId());
        assertEquals(creationDto.getStart(), bookingDto.getStart());
        assertEquals(creationDto.getEnd(), bookingDto.getEnd());
        assertEquals(BookingStatus.WAITING, bookingDto.getStatus());
        assertEquals(creationDto.getItemId(), bookingDto.getItem().getId());
        assertEquals(booker.getId(), bookingDto.getBooker().getId());
    }

    @Test
    public void shouldThrowWhenCreateBooking() {
        // Проверка времени: начало должно быть раньше конца
        final CreateBookingDto creationWrongTimeDto = creationDto.withEnd(LocalDateTime.now().plusMinutes(2));

        final Exception timeException = assertThrows(BadRequestException.class, () -> {
            bookingService.create(booker.getId(), creationWrongTimeDto);
        });
        final String expectedTimeMessage = "The booking start date must be before the end date.";
        final String timeMessage = timeException.getMessage();
        assertTrue(timeMessage.contains(expectedTimeMessage));

        // Проверка бронирующего: несуществующий пользователь
        final Long wrongBookerId = 9999L;
        final Exception nonExistentUserException = assertThrows(NoAccessException.class, () -> {
            bookingService.create(wrongBookerId, creationDto);
        });
        final String expectedNonExistentUserMessage = "You haven't access to booking. Please, log in.";
        final String nonExistentUserMessage = nonExistentUserException.getMessage();
        assertTrue(nonExistentUserMessage.contains(expectedNonExistentUserMessage));

        // Проверка бронируемого предмета: несуществующий предмет
        final Long wrongItemId = 9999L;
        final CreateBookingDto creationWrongItemIdDto = creationDto.withItemId(wrongItemId);

        final Exception itemException = assertThrows(NotFoundException.class, () -> {
            bookingService.create(booker.getId(), creationWrongItemIdDto);
        });
        final String expectedItemMessage = String.format("Item with id %d is not exist.", wrongItemId);
        final String itemMessage = itemException.getMessage();
        assertTrue(itemMessage.contains(expectedItemMessage));

        // Проверка бронирующего: хозяин предмета
        final Exception ownerException = assertThrows(NotFoundException.class, () -> {
            bookingService.create(ownerItem.getId(), creationDto);
        });
        final String expectOwnerMessage = "Item cannot be reserved.";
        final String ownerExceptionMessage = ownerException.getMessage();
        assertTrue(ownerExceptionMessage.contains(expectOwnerMessage));

        // Проверка бронируемого предмета: статус available == false
        item.setAvailable(false);
        final Item itemNotAvailable = itemStorage.save(item);
        final Exception itemStatusException = assertThrows(BadRequestException.class, () -> {
            bookingService.create(booker.getId(), creationDto);
        });
        final String expectedItemStatusMessage = String.format("Item with id %d is not available.",
                itemNotAvailable.getId());
        final String itemStatusMessage = itemStatusException.getMessage();
        assertTrue(itemStatusMessage.contains(expectedItemStatusMessage));
    }

    @Test
    public void shouldUpdateStatusBooking() {
        // Проверка с принятием бронирования
        bookingDto = bookingService.create(booker.getId(), creationDto);
        final BookingDto dtoApproved = bookingService.updateStatus(ownerItem.getId(),
                bookingDto.getId(),
                true);

        final BookingDto expectedApprovedDto = bookingDto.withStatus(BookingStatus.APPROVED);
        assertNotNull(dtoApproved);
        assertNotNull(dtoApproved.getId());
        assertEquals(expectedApprovedDto, dtoApproved);

        // Проверка с отклонением бронирования
        final BookingDto secondBookingDto = bookingService.create(booker.getId(), creationDto);
        assertNotNull(secondBookingDto);

        final BookingDto dtoRejected = bookingService.updateStatus(ownerItem.getId(),
                secondBookingDto.getId(),
                false);
        final BookingDto expectedRejectedDto = secondBookingDto.withStatus(BookingStatus.REJECTED);
        assertNotNull(dtoRejected);
        assertNotNull(dtoRejected.getId());
        assertEquals(expectedRejectedDto, dtoRejected);
    }

    @Test
    public void shouldThrowWhenUpdateStatusBooking() {
        bookingDto = bookingService.create(booker.getId(), creationDto);

        // Проверка бронирования: несуществующее бронирование
        final Long wrongBookingId = 9999L;
        final Exception bookingIdException = assertThrows(NotFoundException.class, () -> {
            bookingService.updateStatus(ownerItem.getId(), wrongBookingId, true);
        });
        final String expectedBookingIdMessage = String.format("Booking with id %d is not exist.", wrongBookingId);
        final String bookingIdMessage = bookingIdException.getMessage();
        assertTrue(bookingIdMessage.contains(expectedBookingIdMessage));

        // Проверка пользователя: изменение статуса бронирования не владельцем предмета
        final Exception ownerIdException = assertThrows(NotFoundException.class, () -> {
            bookingService.updateStatus(booker.getId(), bookingDto.getId(), true);
        });
        final String expectedOwnerIdMessage = "You haven't access to update this booking.";
        final String ownerIdMessage = ownerIdException.getMessage();
        assertTrue(ownerIdMessage.contains(expectedOwnerIdMessage));

        // Проверка статуса бронирования: повторно изменить статус бронирования
        final BookingDto dtoApproved = bookingService.updateStatus(ownerItem.getId(),
                bookingDto.getId(),
                true);
        assertNotNull(dtoApproved);

        final Exception changeStatusAfterAppException = assertThrows(BadRequestException.class, () -> {
            bookingService.updateStatus(ownerItem.getId(), bookingDto.getId(), true);
        });
        final String expectedChangeStatusAfterAppMessage = "The status of this booking has already been changed";
        final String changeStatusAfterAppMessage = changeStatusAfterAppException.getMessage();
        assertTrue(changeStatusAfterAppMessage.contains(expectedChangeStatusAfterAppMessage));

        // Проверка статуса бронирования: повторно изменить статус бронирования
        final BookingDto secondBookingDto = bookingService.create(booker.getId(), creationDto);
        assertNotNull(secondBookingDto);
        final BookingDto dtoRejected = bookingService.updateStatus(ownerItem.getId(),
                secondBookingDto.getId(),
                false);
        assertNotNull(dtoRejected);

        final Exception changeStatusAfterRejException = assertThrows(BadRequestException.class, () -> {
            bookingService.updateStatus(ownerItem.getId(), secondBookingDto.getId(), false);
        });
        final String expectedChangeStatusAfterRejMessage = "The status of this booking has already been changed";
        final String changeStatusAfterRejMessage = changeStatusAfterRejException.getMessage();
        assertTrue(changeStatusAfterRejMessage.contains(expectedChangeStatusAfterRejMessage));
    }

    @Test
    public void shouldDeleteBooking() {
        bookingDto = bookingService.create(booker.getId(), creationDto);
        assertNotNull(bookingDto);

        final Long deletedId = bookingService.delete(booker.getId(), bookingDto.getId());
        assertNotNull(deletedId);
        assertEquals(bookingDto.getId(), deletedId);

        // Проверка получения несуществующего бронирования
        final Exception exception = assertThrows(NotFoundException.class, () -> {
            bookingService.findById(booker.getId(), bookingDto.getId());
        });
        final String expectedMessage = String.format("Booking with id %d is not exist.", bookingDto.getId());
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void shouldThrowWhenDeleteBooking() {
        // Проверка удаления бронирования не бронирующим
        bookingDto = bookingService.create(booker.getId(), creationDto);
        assertNotNull(bookingDto);

        final Exception exception = assertThrows(NoAccessException.class, () -> {
            bookingService.delete(ownerItem.getId(), bookingDto.getId());
        });
        final String expectedMessage = "You haven't access to delete this booking.";
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void shouldFindByIdBooking() {
        // Проверка получения бронирования бронирующим
        bookingDto = bookingService.create(booker.getId(), creationDto);
        assertNotNull(bookingDto);

        final BookingDto receivedBookerDto = bookingService.findById(booker.getId(), bookingDto.getId());
        assertNotNull(receivedBookerDto);
        assertEquals(bookingDto, receivedBookerDto);

        // Проверка получения бронирования валдельцем предмета
        final BookingDto receivedOwnerItemDto = bookingService.findById(booker.getId(), bookingDto.getId());
        assertNotNull(receivedOwnerItemDto);
        assertEquals(bookingDto, receivedOwnerItemDto);
    }

    @Test
    public void shouldThrowWhenFindByIdBooking() {
        bookingDto = bookingService.create(booker.getId(), creationDto);
        assertNotNull(bookingDto);

        // Проверка получения бронирования не владельцем вещи или не бронирующим
        final Long userId = 9999L;
        final Exception exception = assertThrows(NotFoundException.class, () -> {
            bookingService.findById(userId, bookingDto.getId());
        });
        final String expectedMessage = "This booking isn't found.";
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void findAllByBooker() {
        bookingDto = bookingService.create(booker.getId(), creationDto);
        final CreateBookingDto creationSecondDto = creationDto
                .withStart(LocalDateTime.now().plusMinutes(11).truncatedTo(ChronoUnit.MICROS))
                .withEnd(LocalDateTime.now().plusMinutes(16).truncatedTo(ChronoUnit.MICROS));
        final BookingDto secondDto = bookingService.create(booker.getId(), creationSecondDto);
        assertNotNull(bookingDto);
        assertNotNull(secondDto);

        // Ожидаемый список бронирований для проверки текущих, будущих и ожидающих подтверждения
        final List<BookingDto> expectedBookings = new ArrayList<>(List.of(bookingDto, secondDto));
        expectedBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        // Проверка получения текущих бронирований по бронируемому
        final LocalDateTime currentTime = LocalDateTime.now().plusMinutes(12).truncatedTo(ChronoUnit.MICROS);
        final Pageable pageable = PageRequest.of(0,
                5,
                Sort.by(Sort.Direction.DESC, "startDate"));
        List<BookingDto> currentBookings = bookingService.findAllByBooker(booker.getId(),
                "CURRENT",
                currentTime,
                pageable);
        assertNotNull(currentBookings);
        assertEquals(2, currentBookings.size());
        assertEquals(currentBookings, expectedBookings);

        // Проверка получения будущих бронирований по бронируемому
        List<BookingDto> futureBookings = bookingService.findAllByBooker(booker.getId(),
                "FUTURE",
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                pageable);
        assertNotNull(futureBookings);
        assertEquals(2, futureBookings.size());
        assertEquals(futureBookings, expectedBookings);

        // Проверка получения ожидающих подтверждения бронирований по бронируемому
        List<BookingDto> waitingBookings = bookingService.findAllByBooker(booker.getId(),
                "WAITING",
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                pageable);
        assertNotNull(waitingBookings);
        assertEquals(2, waitingBookings.size());
        assertEquals(waitingBookings, expectedBookings);

        // Проверка получения отклонённых бронирований по бронируемому
            // Создание новых бронирований
        final CreateBookingDto creationThirdDto = creationDto
                .withStart(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MICROS))
                .withEnd(LocalDateTime.now().plusMinutes(20).truncatedTo(ChronoUnit.MICROS));
        final CreateBookingDto creationFourthDto = creationDto
                .withStart(LocalDateTime.now().plusMinutes(25).truncatedTo(ChronoUnit.MICROS))
                .withEnd(LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MICROS));

        final BookingDto rejectedFirstDto = bookingService.create(booker.getId(), creationThirdDto);
        final BookingDto rejectedSecondDto = bookingService.create(booker.getId(), creationFourthDto);
        assertNotNull(rejectedFirstDto);
        assertNotNull(rejectedSecondDto);
            // Отклонение новых бронирований
        final BookingDto dtoFirstRejected = bookingService.updateStatus(ownerItem.getId(),
                rejectedFirstDto.getId(),
                false);
        final BookingDto dtoSecondRejected = bookingService.updateStatus(ownerItem.getId(),
                rejectedSecondDto.getId(),
                false);
        assertNotNull(dtoFirstRejected);
        assertNotNull(dtoSecondRejected);

            // Ожидаемый список отклонённых бронирований
        final List<BookingDto> expectedRejectedBookings = new ArrayList<>(List.of(dtoFirstRejected, dtoSecondRejected));
        expectedRejectedBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        List<BookingDto> rejectedBookings = bookingService.findAllByBooker(booker.getId(),
                "REJECTED",
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                pageable);
        assertNotNull(rejectedBookings);
        assertEquals(2, rejectedBookings.size());
        assertEquals(rejectedBookings, expectedRejectedBookings);

        // Проверка получения завершённых бронирований по бронируемому
            // Подтверждение 1 и 2 бронирований
        final BookingDto dtoFirstApproved = bookingService.updateStatus(ownerItem.getId(),
                bookingDto.getId(),
                true);
        final BookingDto dtoSecondApproved = bookingService.updateStatus(ownerItem.getId(),
                secondDto.getId(),
                true);
        assertNotNull(dtoFirstApproved);
        assertNotNull(dtoSecondApproved);

            // Ожидаемый список прошлых бронирований по бронируемому
        final List<BookingDto> expectedPastBookings = new ArrayList<>(List.of(dtoFirstApproved, dtoSecondApproved));
        expectedPastBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));
        List<BookingDto> pastBookings = bookingService.findAllByBooker(booker.getId(),
                "PAST",
                LocalDateTime.now().plusMinutes(20).truncatedTo(ChronoUnit.MICROS),
                pageable);
        assertNotNull(pastBookings);
        assertEquals(2, pastBookings.size());
        assertEquals(expectedPastBookings, pastBookings);

        // Проверка получения всех бронирований по бронируемому
            // Ожидаемый список всех бронирований
        final List<BookingDto> expectedAllBookings = new ArrayList<>();
        expectedAllBookings.addAll(expectedRejectedBookings);
        expectedAllBookings.addAll(expectedPastBookings);
        expectedAllBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        List<BookingDto> allBookings = bookingService.findAllByBooker(booker.getId(),
                "ALL",
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                pageable);
        assertNotNull(allBookings);
        assertEquals(4, allBookings.size());
        assertEquals(expectedAllBookings, allBookings);
    }

    @Test
    public void shouldReturnCorrectPaginationByBooker() {
        final LocalDateTime currentTime = LocalDateTime.now();
        final Sort sort = Sort.by(Sort.Direction.DESC, "startDate");

        bookingDto = bookingService.create(booker.getId(), creationDto);
        final CreateBookingDto creationSecondDto = creationDto
                .withStart((LocalDateTime.now().plusMinutes(20).truncatedTo(ChronoUnit.MICROS)))
                .withEnd((LocalDateTime.now().plusMinutes(25).truncatedTo(ChronoUnit.MICROS)));
        final CreateBookingDto creationThirdDto = creationDto
                .withStart((LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MICROS)))
                .withEnd((LocalDateTime.now().plusMinutes(35).truncatedTo(ChronoUnit.MICROS)));
        final BookingDto secondDto = bookingService.create(booker.getId(), creationSecondDto);
        final BookingDto thirdDto = bookingService.create(booker.getId(), creationThirdDto);
        assertNotNull(bookingDto);
        assertNotNull(secondDto);
        assertNotNull(thirdDto);

        final List<BookingDto> expectedTwoBookings = new ArrayList<>(List.of(secondDto, thirdDto));
        expectedTwoBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        // Проверка пагинации с 0 элемента, размер страниц = 2
        final Pageable pageableAt0 = PageRequest.of(0,
                2,
                sort);
        List<BookingDto> bookingsAt0 = bookingService.findAllByBooker(booker.getId(),
                "ALL",
                currentTime,
                pageableAt0);
        assertNotNull(bookingsAt0);
        assertEquals(2, bookingsAt0.size());
        assertEquals(bookingsAt0, expectedTwoBookings);

        // Проверка пагинации с 3 элемента, размер страниц = 2
        final Pageable pageableAt3 = PageRequest.of(3 / 2, 2, sort);
        List<BookingDto> bookingsAt3 = bookingService.findAllByBooker(booker.getId(),
                "ALL",
                currentTime,
                pageableAt3);
        assertNotNull(bookingsAt3);
        assertEquals(1, bookingsAt3.size());
        assertEquals(bookingsAt3, new ArrayList<>(List.of(bookingDto)));
    }

    @Test
    public void shouldThrowWhenFindAllByBooker() {
        final LocalDateTime currentTime = LocalDateTime.now();
        final Pageable pageable = PageRequest.of(0,
                5,
                Sort.by(Sort.Direction.DESC, "startDate"));

        // Проверка получения бронирования неизвестным бронирующим
        final Long wrongUserId = 9999L;
        final Exception wrongUserException = assertThrows(NoAccessException.class, () -> {
            bookingService.findAllByBooker(wrongUserId, "ALL", currentTime, pageable);
        });
        final String expectedWrongUserMessage = "You haven't access to booking. Please, log in.";
        final String wrongUserMessage = wrongUserException.getMessage();
        assertTrue(wrongUserMessage.contains(expectedWrongUserMessage));

        // Проверка получения бронирований бронирующим по неизвестному статусу
        final Exception wrongStatusException = assertThrows(UnsupportedStatusException.class, () -> {
            bookingService.findAllByBooker(booker.getId(), "FOO", currentTime, pageable);
        });
        final String expectedWrongStatusMessage = "Unknown state: UNSUPPORTED_STATUS";
        final String wrongStatusMessage = wrongStatusException.getMessage();
        assertTrue(wrongStatusMessage.contains(expectedWrongStatusMessage));
    }

    @Test
    public void shouldFindAllByOwnerItemsBooking() {
        final Pageable pageable = PageRequest.of(0,
                5,
                Sort.by(Sort.Direction.DESC, "startDate"));

        // Проверка с пустым списком
        List<BookingDto> emptyBookings = bookingService.findAllByOwnerItems(booker.getId(),
                "All",
                LocalDateTime.now(),
                pageable);
        assertNotNull(emptyBookings);
        assertTrue(emptyBookings.isEmpty());

        final Item secondItem = itemStorage.save(Item.builder()
                .name("second item")
                .description("second description")
                .available(true)
                .ownerId(ownerItem.getId())
                .build());
        assertNotNull(secondItem);

        final CreateBookingDto creationSecondDto = CreateBookingDto.builder()
                .start(LocalDateTime.now().plusMinutes(11).truncatedTo(ChronoUnit.MICROS))
                .end(LocalDateTime.now().plusMinutes(16).truncatedTo(ChronoUnit.MICROS))
                .itemId(secondItem.getId())
                .build();
        bookingDto = bookingService.create(booker.getId(), creationDto);
        final BookingDto secondBookingDto = bookingService.create(booker.getId(), creationSecondDto);
        assertNotNull(bookingDto);
        assertNotNull(secondBookingDto);

            // Ожидаемый список бронирований для проверки текущих, будущих и ожидающих подтверждения
        final List<BookingDto> expectedBookings = new ArrayList<>(List.of(bookingDto, secondBookingDto));
        expectedBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        // Проверка получения текущих бронирований по владельцу вещи
        final LocalDateTime currentTime = LocalDateTime.now().plusMinutes(12);
        List<BookingDto> currentBookings = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "CURRENT",
                currentTime,
                pageable);
        assertNotNull(currentBookings);
        assertEquals(2, currentBookings.size());
        assertEquals(currentBookings, expectedBookings);

        // Проверка получения будущих бронирований по владельцу вещи
        List<BookingDto> futureBookings = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "FUTURE",
                LocalDateTime.now(),
                pageable);
        assertNotNull(futureBookings);
        assertEquals(2, futureBookings.size());
        assertEquals(futureBookings, expectedBookings);

        // Проверка получения ожидающих подтверждения бронирований по владельцу вещи
        List<BookingDto> waitingBookings = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "WAITING",
                LocalDateTime.now(),
                pageable);
        assertNotNull(waitingBookings);
        assertEquals(2, waitingBookings.size());
        assertEquals(waitingBookings, expectedBookings);

        // Проверка получения отклонённых бронирований по владельцу вещи
            // Создание новых бронирований
        final CreateBookingDto creationThirdDto = CreateBookingDto.builder()
                .start(LocalDateTime.now().plusMinutes(15).truncatedTo(ChronoUnit.MICROS))
                .end(LocalDateTime.now().plusMinutes(20).truncatedTo(ChronoUnit.MICROS))
                .itemId(item.getId())
                .build();
        final CreateBookingDto creationFourthDto = CreateBookingDto.builder()
                .start(LocalDateTime.now().plusMinutes(25).truncatedTo(ChronoUnit.MICROS))
                .end(LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MICROS))
                .itemId(secondItem.getId())
                .build();

        final BookingDto rejectedFirstDto = bookingService.create(booker.getId(), creationThirdDto);
        final BookingDto rejectedSecondDto = bookingService.create(booker.getId(), creationFourthDto);
        assertNotNull(rejectedFirstDto);
        assertNotNull(rejectedSecondDto);
            // Отклонение новых бронирований
        final BookingDto dtoFirstRejected = bookingService.updateStatus(ownerItem.getId(),
                rejectedFirstDto.getId(),
                false);
        final BookingDto dtoSecondRejected = bookingService.updateStatus(ownerItem.getId(),
                rejectedSecondDto.getId(),
                false);
        assertNotNull(dtoFirstRejected);
        assertNotNull(dtoSecondRejected);

            // Ожидаемый список отклонённых бронирований по владельцу вещи
        final List<BookingDto> expectedRejectedBookings = new ArrayList<>(List.of(dtoFirstRejected, dtoSecondRejected));
        expectedRejectedBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        List<BookingDto> rejectedBookings = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "REJECTED",
                LocalDateTime.now(),
                pageable);
        assertNotNull(rejectedBookings);
        assertEquals(2, rejectedBookings.size());
        assertEquals(rejectedBookings, expectedRejectedBookings);

        // Проверка получения завершённых бронирований по владельцу вещи
            // Подтверждение 1 и 2 бронирований
        final BookingDto dtoFirstApproved = bookingService.updateStatus(ownerItem.getId(),
                bookingDto.getId(),
                true);
        final BookingDto dtoSecondApproved = bookingService.updateStatus(ownerItem.getId(),
                secondBookingDto.getId(),
                true);
        assertNotNull(dtoFirstApproved);
        assertNotNull(dtoSecondApproved);

            // Ожидаемый список прошлых бронирований по владельцу вещи
        final List<BookingDto> expectedPastBookings = new ArrayList<>(List.of(dtoFirstApproved, dtoSecondApproved));
        expectedPastBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        List<BookingDto> pastBookings = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "PAST",
                LocalDateTime.now().plusMinutes(20),
                pageable);
        assertNotNull(pastBookings);
        assertEquals(2, pastBookings.size());
        assertEquals(pastBookings, expectedPastBookings);

        // Проверка получения всех бронирований по владельцу вещи
            // Ожидаемый список всех бронирований
        final List<BookingDto> expectedAllBookings = new ArrayList<>();
        expectedAllBookings.addAll(expectedRejectedBookings);
        expectedAllBookings.addAll(expectedPastBookings);
        expectedAllBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        List<BookingDto> allBookings = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "ALL",
                LocalDateTime.now(),
                pageable);
        assertNotNull(allBookings);
        assertEquals(4, allBookings.size());
        assertEquals(allBookings, expectedAllBookings);
    }

    @Test
    public void shouldReturnCorrectPaginationByOwnerItemsBooking() {
        final LocalDateTime currentTime = LocalDateTime.now();
        final Sort sort = Sort.by(Sort.Direction.DESC, "startDate");

        bookingDto = bookingService.create(booker.getId(), creationDto);
        final CreateBookingDto creationSecondDto = creationDto
                .withStart((LocalDateTime.now().plusMinutes(20).truncatedTo(ChronoUnit.MICROS)))
                .withEnd((LocalDateTime.now().plusMinutes(25).truncatedTo(ChronoUnit.MICROS)));
        final CreateBookingDto creationThirdDto = creationDto
                .withStart((LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MICROS)))
                .withEnd((LocalDateTime.now().plusMinutes(35).truncatedTo(ChronoUnit.MICROS)));
        final BookingDto secondDto = bookingService.create(booker.getId(), creationSecondDto);
        final BookingDto thirdDto = bookingService.create(booker.getId(), creationThirdDto);
        assertNotNull(bookingDto);
        assertNotNull(secondDto);
        assertNotNull(thirdDto);

        final List<BookingDto> expectedTwoBookings = new ArrayList<>(List.of(secondDto, thirdDto));
        expectedTwoBookings.sort(Comparator.comparing(BookingDto::getStart, Comparator.reverseOrder()));

        // Проверка пагинации с 0 элемента, размер страниц = 2
        final Pageable pageableAt0 = PageRequest.of(0,
                2,
                sort);
        List<BookingDto> bookingsAt0 = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "ALL",
                currentTime,
                pageableAt0);
        assertNotNull(bookingsAt0);
        assertEquals(2, bookingsAt0.size());
        assertEquals(bookingsAt0, expectedTwoBookings);

        // Проверка пагинации с 3 элемента, размер страниц = 2
        final Pageable pageableAt3 = PageRequest.of(3 / 2, 2, sort);
        List<BookingDto> bookingsAt3 = bookingService.findAllByOwnerItems(ownerItem.getId(),
                "ALL",
                currentTime,
                pageableAt3);
        assertNotNull(bookingsAt3);
        assertEquals(1, bookingsAt3.size());
        assertEquals(bookingsAt3, new ArrayList<>(List.of(bookingDto)));
    }

    @Test
    public void shouldThrowWhenFindAllByOwnerItemsBooking() {
        final LocalDateTime currentTime = LocalDateTime.now();
        final Pageable pageable = PageRequest.of(0,
                5,
                Sort.by(Sort.Direction.DESC, "startDate"));

        // Проверка получения бронирований неизвестным пользователем
        final Long wrongUserId = 9999L;
        final Exception wrongUserException = assertThrows(NoAccessException.class, () -> {
            bookingService.findAllByOwnerItems(wrongUserId, "ALL", currentTime, pageable);
        });
        final String expectedWrongUserMessage = "You haven't access to booking. Please, log in.";
        final String wrongUserMessage = wrongUserException.getMessage();
        assertTrue(wrongUserMessage.contains(expectedWrongUserMessage));

        // Проверка получения бронирований владельцем вещей по неизвестному статусу
        final Exception wrongStatusException = assertThrows(UnsupportedStatusException.class, () -> {
            bookingService.findAllByOwnerItems(ownerItem.getId(), "FOO", currentTime, pageable);
        });
        final String expectedWrongStatusMessage = "Unknown state: UNSUPPORTED_STATUS";
        final String wrongStatusMessage = wrongStatusException.getMessage();
        assertTrue(wrongStatusMessage.contains(expectedWrongStatusMessage));
    }
}