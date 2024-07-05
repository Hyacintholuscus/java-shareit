package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final CommentStorage commentStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final ItemRequestStorage requestStorage;
    private final ItemMapper mapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    private void checkUserId(Long userId) {
        if (!userStorage.existsById(userId)) {
            log.error("NotFound. Запрос на действие с предметом от несуществующего пользователя с id {}.", userId);
            throw new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        }
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    private List<ItemDto> getListItemDtoWithBookings(List<Item> items, LocalDateTime currentTime) {
        if (items.isEmpty()) return new ArrayList<>();
        Set<Long> itemId = items.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        List<Booking> bookingList = bookingStorage.findLastAndNextForItem(itemId, currentTime);
        if (bookingList.isEmpty()) {
            return items.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
        }
        Map<Long, List<Booking>> bookings = bookingList.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        LinkedList<ItemDto> dtos = new LinkedList<>();
        for (Item item : items) {
            ItemDto dto;
            List<Booking> itemBookings = bookings.get(item.getId());
            BookingItemDto lastBooking = null;
            BookingItemDto nextBooking = null;
            if (itemBookings == null) {
                dto = mapper.toDto(item);
            } else {
                for (Booking booking : itemBookings) {
                    if ((booking.getEndDate().isBefore(currentTime)
                            || (booking.getStartDate().isBefore(currentTime)
                            && booking.getEndDate().isAfter(currentTime)))) {
                        lastBooking = bookingMapper.toItemDto(booking);
                    } else nextBooking = bookingMapper.toItemDto(booking);
                }
                dto = mapper.toDto(item, lastBooking, nextBooking);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Запрос создать вещь от пользователя с id {}", userId);

        checkUserId(userId);
        if (itemDto.getRequestId() != null) {
            if (!requestStorage.existsById(itemDto.getRequestId())) {
                log.error("NotFound. Запрос на создание предмета в ответ на несуществующий запрос с id {}.",
                        itemDto.getRequestId());
                throw new NotFoundException(
                        String.format("ItemRequest with id %d is not exist.", itemDto.getRequestId()));
            }
        }
        Item item = mapper.toItem(userId, itemDto);
        return mapper.toDto(itemStorage.save(item));
    }

    @Override
    public CommentDto createComment(Long itemId,
                                    Long authorId,
                                    LocalDateTime createdTime,
                                    CreateCommentDto commentRequestDto) {
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAllByUser(Long userId, LocalDateTime currentTime, Pageable pageable) {
        log.info("Запрос получить список вещей от пользователя с id {}", userId);

        User user = userStorage.findById(userId).orElseThrow(() -> {
            log.error("NotFound. Запрос на получение списка предметов от несуществующего пользователя с id {}.", userId);
            return new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        });

        int from = Math.toIntExact(pageable.getOffset());
        int size = Math.min((from + pageable.getPageSize()), user.getItems().size());

        List<Item> items = user.getItems().subList(from, size);
        items.sort(Comparator.comparing(Item::getId));
        return getListItemDtoWithBookings(items, currentTime);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(Long userId, String text, Pageable pageable) {
        log.info("Запрос на поиск вещей от пользователя с id {}", userId);

        checkUserId(userId);
        return itemStorage.search(text, pageable).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
