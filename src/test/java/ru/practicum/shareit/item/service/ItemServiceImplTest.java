package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final ItemService itemService;
    private final ItemStorage itemStorage;
    private final ItemMapper itemMapper;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final BookingMapper bookingMapper;
    private final ItemRequestStorage requestStorage;

    private Item itemWithBookings;
    private Item itemWithoutBookings;
    private User owner;
    private User user;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void beforeEach() {
        owner = userStorage.save(User.builder()
                .name("owner")
                .email("owner@email.ru")
                .build());
        user = userStorage.save(User.builder()
                .name("user")
                .email("user@email.ru")
                .build());
        itemWithBookings = itemStorage.save(Item.builder()
                .name("item booking")
                .description("description")
                .available(true)
                .ownerId(owner.getId())
                .build());
        itemWithoutBookings = itemStorage.save(Item.builder()
                .name("item without booking")
                .description("description")
                .available(true)
                .ownerId(owner.getId())
                .build());
        lastBooking = bookingStorage.save(Booking.builder()
                .startDate(LocalDateTime.now().minusMinutes(30).truncatedTo(ChronoUnit.MICROS))
                .endDate(LocalDateTime.now().minusMinutes(20).truncatedTo(ChronoUnit.MICROS))
                .item(itemWithBookings)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build());
        nextBooking = bookingStorage.save(Booking.builder()
                .startDate(LocalDateTime.now().plusMinutes(20).truncatedTo(ChronoUnit.MICROS))
                .endDate(LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MICROS))
                .item(itemWithBookings)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build());
    }

    private ItemDto createItemDtoWithoutRequest() {
        return ItemDto.builder()
                .name("item")
                .description("description")
                .available(true)
                .comments(new ArrayList<>())
                .build();
    }

    private CreateCommentDto getForCreateCommentDto() {
        return CreateCommentDto.builder()
                .text("comment")
                .build();
    }

    @Test
    public void shouldCreateItem() {
        // Создание предмета без requestId
        final ItemDto itemDto = createItemDtoWithoutRequest();
        final ItemDto createdDto = itemService.create(owner.getId(), itemDto);
        final ItemDto expectedDto = itemDto.withId(createdDto.getId());

        assertNotNull(createdDto);
        assertNotNull(createdDto.getId());
        assertEquals(expectedDto, createdDto);

        // Создание предмета с requestId
        final ItemRequest itemRequest = ItemRequest.builder()
                .description("itemRequest description")
                .creationDate(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .ownerId(user.getId())
                .build();
        final ItemRequest createdItemRequest = requestStorage.save(itemRequest);
        assertNotNull(createdItemRequest);

        final ItemDto dtoWithRequestId = itemDto.withRequestId(createdItemRequest.getId());
        final ItemDto createdDtoWithRequestId = itemService.create(owner.getId(), dtoWithRequestId);
        final ItemDto expectedDtoWithRequestId = dtoWithRequestId.withId(createdDtoWithRequestId.getId());
        assertNotNull(createdDtoWithRequestId);
        assertNotNull(createdDtoWithRequestId.getId());
        assertEquals(expectedDtoWithRequestId, createdDtoWithRequestId);
    }

    @Test
    public void shouldThrowWhenCreateItem() {
        final ItemDto itemDto = createItemDtoWithoutRequest();

        // Проверка владельца предмета: несуществующий пользователь
        final Long wrongOwnerId = 9999L;
        final Exception nonExistentUserException = assertThrows(NotFoundException.class, () -> {
            itemService.create(wrongOwnerId, itemDto);
        });
        final String expectedNonExistentUserMessage = String.format("User with id %d is not exist.", wrongOwnerId);
        final String nonExistentUserMessage = nonExistentUserException.getMessage();
        assertTrue(nonExistentUserMessage.contains(expectedNonExistentUserMessage));

        // Проверка запроса: несуществующий ItemRequest
        final Long wrongRequestId = 9999L;
        final ItemDto dtoWithWrongRequestId = itemDto.withRequestId(wrongRequestId);
        final Exception requestException = assertThrows(NotFoundException.class, () -> {
            itemService.create(owner.getId(), dtoWithWrongRequestId);
        });
        final String expectedRequestMessage = String.format("ItemRequest with id %d is not exist.",
                wrongRequestId);
        final String requestMessage = requestException.getMessage();
        assertTrue(requestMessage.contains(expectedRequestMessage));
    }

    @Test
    public void shouldCreateComment() {
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final CreateCommentDto creationCommentDto = getForCreateCommentDto();
        final CommentDto commentDto = itemService.createComment(itemWithBookings.getId(), user.getId(),
                creationTime, creationCommentDto);

        assertNotNull(commentDto);
        assertEquals(creationCommentDto.getText(), commentDto.getText());
        assertEquals(user.getName(), commentDto.getAuthorName());
        assertEquals(creationTime, commentDto.getCreated());

        // Проверка получения предмета с комментариями
        final ItemDto dtoWithComment = itemService.getById(owner.getId(), itemWithBookings.getId(),
                LocalDateTime.now());
        assertNotNull(dtoWithComment);
        assertEquals(List.of(commentDto), dtoWithComment.getComments());
    }

    @Test
    public void shouldThrowWhenCreateComment() {
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final CreateCommentDto creationCommentDto = getForCreateCommentDto();

        final Exception commentException = assertThrows(BadRequestException.class, () -> {
            itemService.createComment(itemWithoutBookings.getId(), user.getId(),
                    creationTime, creationCommentDto);
        });
        final String expectedCommentMessage = "You cannot leave a comment on this item.";
        final String commentMessage = commentException.getMessage();
        assertTrue(commentMessage.contains(expectedCommentMessage));
    }

    @Test
    public void shouldUpdateItem() {
        // Проверка обновления полей Item без бронирований
        final Map<String, Object> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("name", "item updated");
        fieldsToUpdate.put("description", "description updated");
        fieldsToUpdate.put("available", false);

        final ItemDto dtoWithoutBooking = itemService.getById(owner.getId(), itemWithoutBookings.getId(),
                LocalDateTime.now());
        final ItemDto expectedDto = dtoWithoutBooking.withName("item updated")
                .withDescription("description updated")
                .withAvailable(false);

        final ItemDto updatedDto = itemService.update(itemWithoutBookings.getId(), owner.getId(),
                fieldsToUpdate, LocalDateTime.now());

        assertNotNull(updatedDto);
        assertEquals(expectedDto, updatedDto);

        // Проверка обновления полей Item с бронированиями
        final Map<String, Object> fieldsToUpdateWithBooking = new HashMap<>();
        fieldsToUpdateWithBooking.put("name", "item booking updated");
        fieldsToUpdateWithBooking.put("description", "description updated");
        fieldsToUpdateWithBooking.put("available", false);

        final ItemDto dtoWithBooking = itemService.getById(owner.getId(), itemWithBookings.getId(), LocalDateTime.now());
        final ItemDto expectedWithBookingDto = dtoWithBooking.withName("item booking updated")
                .withDescription("description updated")
                .withAvailable(false);

        final ItemDto updatedDtoWithBooking = itemService.update(itemWithBookings.getId(), owner.getId(),
                fieldsToUpdateWithBooking, LocalDateTime.now());

        assertNotNull(updatedDtoWithBooking);
        assertEquals(expectedWithBookingDto, updatedDtoWithBooking);
    }

    @Test
    public void shouldThrowWhenUpdateItem() {
        final Map<String, Object> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("name", "item updated");
        fieldsToUpdate.put("description", "description updated");
        fieldsToUpdate.put("available", false);

        final Exception commentException = assertThrows(NoAccessException.class, () -> {
            itemService.update(itemWithoutBookings.getId(), user.getId(),
                    fieldsToUpdate, LocalDateTime.now());
        });
        final String expectedCommentMessage = "You haven't access to update this item.";
        final String commentMessage = commentException.getMessage();
        assertTrue(commentMessage.contains(expectedCommentMessage));
    }

    @Test
    public void shouldDeleteItem() {
        // Проверка удаления предмета без бронирований
        final Long deletedId = itemService.delete(owner.getId(), itemWithoutBookings.getId());

        assertNotNull(deletedId);
        assertEquals(itemWithoutBookings.getId(), deletedId);

        // Проверка удаления предмета с бронированиями
        final Long deletedWithBookingId = itemService.delete(owner.getId(), itemWithBookings.getId());

        assertNotNull(deletedWithBookingId);
        assertEquals(itemWithBookings.getId(), deletedWithBookingId);
            // Проверяется, удалились ли бронирования
        final Booking nullLastBooking = bookingStorage.findById(lastBooking.getId()).orElse(null);
        final Booking nullNextBooking = bookingStorage.findById(nextBooking.getId()).orElse(null);

        assertNull(nullLastBooking);
        assertNull(nullNextBooking);
    }

    @Test
    public void shouldThrowWhenDeleteItem() {
        final Exception commentException = assertThrows(NoAccessException.class, () -> {
            itemService.delete(user.getId(), itemWithoutBookings.getId());
        });
        final String expectedCommentMessage = "You haven't access to delete this item.";
        final String commentMessage = commentException.getMessage();
        assertTrue(commentMessage.contains(expectedCommentMessage));
    }

    @Test
    public void shouldGetByIdItem() {
        // Проверка получения предмета без бронирований
        final ItemDto expectedDto = itemMapper.toDto(itemWithoutBookings);
        final ItemDto savedDto = itemService.getById(owner.getId(),
                itemWithoutBookings.getId(), LocalDateTime.now());

        assertNotNull(savedDto);
        assertEquals(expectedDto, savedDto);

        // Проверка получения предмета с бронированиями
        final BookingItemDto lastBookingItem = bookingMapper.toItemDto(lastBooking);
        final BookingItemDto nextBookingItem = bookingMapper.toItemDto(nextBooking);
        final ItemDto expectedWithBookingDto = itemMapper.toDto(itemWithBookings,
                lastBookingItem, nextBookingItem);
        final ItemDto savedWithBookingDto = itemService.getById(owner.getId(),
                itemWithBookings.getId(), LocalDateTime.now());

        assertNotNull(savedWithBookingDto);
        assertEquals(expectedWithBookingDto, savedWithBookingDto);

        //Проверка получения предмета с бронированиями не владельцем предмета
        final ItemDto expectedWithoutBookingDto = itemMapper.toDto(itemWithBookings);
        final ItemDto savedWithoutBookingDto = itemService.getById(user.getId(),
                itemWithBookings.getId(), LocalDateTime.now());

        assertNotNull(savedWithoutBookingDto);
        assertEquals(expectedWithoutBookingDto, savedWithoutBookingDto);
    }

    @Test
    public void shouldThrowWhenGetByIdItem() {
        final Long wrongItemId = 9999L;
        final Exception commentException = assertThrows(NotFoundException.class, () -> {
            itemService.getById(owner.getId(), wrongItemId, LocalDateTime.now());
        });
        final String expectedCommentMessage = String.format("Item with id %d is not exist.", wrongItemId);
        final String commentMessage = commentException.getMessage();
        assertTrue(commentMessage.contains(expectedCommentMessage));
    }

    private ItemDto createNewItemDtoWithBookingsAndComment(LocalDateTime currentTime) {
        final Item newItem = itemStorage.save(Item.builder()
                .name("item without booking 2")
                .description("description 2")
                .available(true)
                .ownerId(owner.getId())
                .build());
        final Booking newLastBooking = bookingStorage.save(Booking.builder()
                .startDate(LocalDateTime.now().minusMinutes(40).truncatedTo(ChronoUnit.MICROS))
                .endDate(LocalDateTime.now().minusMinutes(30).truncatedTo(ChronoUnit.MICROS))
                .item(newItem)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .build());
        final Booking newNextBooking = bookingStorage.save(Booking.builder()
                .startDate(LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MICROS))
                .endDate(LocalDateTime.now().plusMinutes(40).truncatedTo(ChronoUnit.MICROS))
                .item(newItem)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build());
        final CommentDto commentDto = itemService.createComment(newItem.getId(), user.getId(),
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS), getForCreateCommentDto());
        return itemService.getById(owner.getId(), newItem.getId(), currentTime);
    }

    @Test
    public void shouldGetAllByUserItem() {
        // Проверка получения пустого списка
        final Pageable pageable = PageRequest.of(0, 5);
        final LocalDateTime currentTime = LocalDateTime.now().minusMinutes(25);

        final List<ItemDto> emptyList = itemService.getAllByUser(user.getId(), LocalDateTime.now(), pageable);
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());

        final ItemDto dtoWithoutBookings = itemService.getById(owner.getId(),
                itemWithoutBookings.getId(), currentTime);
        final ItemDto dtoWithBookings = itemService.getById(owner.getId(),
                itemWithBookings.getId(), currentTime);

        final ItemDto dtoWithComment = createNewItemDtoWithBookingsAndComment(currentTime);

        // Проверка получения списка предметов
        final List<ItemDto> savedItemsDto = itemService.getAllByUser(owner.getId(), currentTime, pageable);

        assertNotNull(savedItemsDto);
        assertEquals(3, savedItemsDto.size());
        assertTrue(savedItemsDto.containsAll(List.of(dtoWithComment, dtoWithoutBookings, dtoWithBookings)));
    }

    @Test
    public void shouldThrowWhenGetAllByUserItem() {
        final Long wrongUserId = 9999L;
        final Pageable pageable = PageRequest.of(0, 5);
        final Exception commentException = assertThrows(NotFoundException.class, () -> {
            itemService.getAllByUser(wrongUserId, LocalDateTime.now(), pageable);
        });
        final String expectedCommentMessage = String.format("User with id %d is not exist.", wrongUserId);
        final String commentMessage = commentException.getMessage();
        assertTrue(commentMessage.contains(expectedCommentMessage));
    }

    @Test
    public void shouldSearchItem() {
        final Pageable pageable = PageRequest.of(0, 5);
        final ItemDto dtoWithoutBookings = itemService.getById(user.getId(),
                itemWithoutBookings.getId(), LocalDateTime.now());
        final ItemDto dtoWithBookings = itemService.getById(user.getId(),
                itemWithBookings.getId(), LocalDateTime.now());

        // Проверка игнорирования регистра
        final List<ItemDto> searchItEmDto = itemService.search("ItEm", pageable);

        assertNotNull(searchItEmDto);
        assertEquals(2, searchItEmDto.size());
        assertTrue(searchItEmDto.containsAll(List.of(dtoWithoutBookings, dtoWithBookings)));

        // Проверка поиска конкретного предмета
        final List<ItemDto> searchWithoutDto = itemService.search("without", pageable);

        assertNotNull(searchWithoutDto);
        assertEquals(1, searchWithoutDto.size());
        assertTrue(searchWithoutDto.contains(dtoWithoutBookings));
    }

    @Test
    public void shouldReturnCorrectPagination() {
        final ItemDto dtoWithoutBookings = itemService.getById(owner.getId(),
                itemWithoutBookings.getId(), LocalDateTime.now());
        final ItemDto dtoWithBookings = itemService.getById(owner.getId(),
                itemWithBookings.getId(), LocalDateTime.now());

        final ItemDto itemDto = createItemDtoWithoutRequest();
        final ItemDto thirdDto = itemService.create(owner.getId(), itemDto);

        assertNotNull(thirdDto);

        // Проверка с getAllByUser
        final Pageable pageableAt0 = PageRequest.of(0, 2);
        final List<ItemDto> itemsTwoDto = itemService.getAllByUser(owner.getId(), LocalDateTime.now(), pageableAt0);

        assertNotNull(itemsTwoDto);
        assertEquals(2, itemsTwoDto.size());
        assertTrue(itemsTwoDto.containsAll(List.of(dtoWithoutBookings, dtoWithBookings)));

        final Pageable pageableAt3 = PageRequest.of(3 / 2, 2);
        final List<ItemDto> itemsOneDto = itemService.getAllByUser(owner.getId(), LocalDateTime.now(), pageableAt3);

        assertNotNull(itemsOneDto);
        assertEquals(1, itemsOneDto.size());
        assertTrue(itemsOneDto.contains(thirdDto));

        // Проверка с search
        final List<ItemDto> searchTwoDto = itemService.search("item", pageableAt0);

        final ItemDto dtoWithBookingsWithout = itemService.getById(user.getId(),
                itemWithBookings.getId(), LocalDateTime.now());
        assertNotNull(searchTwoDto);
        assertEquals(2, searchTwoDto.size());
        assertTrue(searchTwoDto.containsAll(List.of(dtoWithoutBookings, dtoWithBookingsWithout)));

        final List<ItemDto> searchOneDto = itemService.search("item", pageableAt3);

        assertNotNull(searchOneDto);
        assertEquals(1, searchOneDto.size());
        assertTrue(searchOneDto.contains(thirdDto));
    }
}