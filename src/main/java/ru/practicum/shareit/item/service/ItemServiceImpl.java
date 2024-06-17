package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final CommentStorage commentStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final ItemMapper mapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    private void checkUserId(Long userId) {
        if (!userStorage.existsById(userId)) {
            log.error("NotFound. Запрос на действие с предметом от несуществующего пользователя с id {}.", userId);
            throw new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        }
    }

    private ItemDto getItemDtoWithBookings(Item item, LocalDateTime currentTime) {
        List<Booking> bookings = bookingStorage.findLastAndNextForItem(item.getId(), currentTime);
        if (bookings.isEmpty()) {
            return mapper.toDto(item);
        }
        BookingItemDto lastBooking = null;
        BookingItemDto nextBooking = null;
        for (Booking booking : bookings) {
            if ((booking.getEndDate().isBefore(currentTime)
                    || (booking.getStartDate().isBefore(currentTime) && booking.getEndDate().isAfter(currentTime)))) {
                lastBooking = bookingMapper.toItemDto(booking);
            } else nextBooking = bookingMapper.toItemDto(booking);
        }
        return mapper.toDto(item, lastBooking, nextBooking);
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Запрос создать вещь от пользователя с id {}", userId);

        checkUserId(userId);
        Item item = mapper.toItem(userId, itemDto);
        return mapper.toDto(itemStorage.save(item));
    }

    @Override
    public CommentDto createComment(Long itemId,
                                    Long authorId,
                                    LocalDateTime createdTime,
                                    CommentRequestDto commentRequestDto) {
        log.info("Запрос создать комментарий на вещь с id{} от пользователя с id {}", itemId, authorId);

        List<Booking> bookings = bookingStorage.findBookingToComment(authorId, itemId, createdTime);
        if (bookings.isEmpty()) {
            log.error("BadRequest. Запрос пользователя с id {} " +
                    "добавить комментарий к вещи с id {}.", authorId, itemId);
            throw new BadRequestException("You cannot leave a comment on this item.");
        }
        Comment comment = commentMapper.toComment(itemId,
                bookings.get(0).getBooker(),
                createdTime,
                commentRequestDto);
        return commentMapper.toDto(commentStorage.save(comment));
    }

    @Override
    public ItemDto update(Long itemId, Long userId, Map<String, Object> fields, LocalDateTime currentTime) {
        log.info("Запрос изменить параметры вещи с id {} от пользователя с id {}", itemId, userId);

        Item item = itemStorage.findItemByOwnerId(userId, itemId).orElseThrow(() -> {
            log.error("NoAccess. Запрос пользователя с id {} на обновление предмета с id {}.", userId, itemId);
            return new NoAccessException("You haven't access to update this item.");
        });
        fields.remove("id");
        fields.forEach((k, v) -> {
            Field field = ReflectionUtils.findField(Item.class, k);
            field.setAccessible(true);
            ReflectionUtils.setField(field, item, v);
        });
        return getItemDtoWithBookings(itemStorage.save(item), currentTime);
    }

    @Override
    public Long delete(Long userId, Long itemId) {
        log.info("Запрос удалить вещь с id {} от пользователя с id {}", itemId, userId);

        Optional<Item> optionalItem = itemStorage.findById(itemId);
        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            if (item.getOwnerId().equals(userId)) {
                itemStorage.deleteById(itemId);
            } else {
                log.error("NoAccess. Запрос пользователя с id {} на удаление предмета с id {}.", userId, itemId);
                throw new NoAccessException("You haven't access to delete this item.");
            }
        }
        return itemId;
    }

    @Override
    public ItemDto getById(Long userId, Long itemId, LocalDateTime currentTime) {
        log.info("Запрос пользователя с id {} на получение предмета с id {}.", userId, itemId);

        Item item = itemStorage.findById(itemId).orElseThrow(() -> {
            log.error("NotFound. Запрос получить несуществующий предмет с id {}.", itemId);
            return new NotFoundException(
                    String.format("Item with id %d is not exist.", itemId)
            );
        });
        if (item.getOwnerId().equals(userId)) {
            return getItemDtoWithBookings(item, currentTime);
        } else return mapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByUser(Long userId, LocalDateTime currentTime) {
        log.info("Запрос получить список вещей от пользователя с id {}", userId);

        checkUserId(userId);
        return itemStorage.findAllByOwnerId(userId).stream()
                .map(item -> getItemDtoWithBookings(item, currentTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Запрос на поиск вещей");

        return itemStorage.search(text).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
