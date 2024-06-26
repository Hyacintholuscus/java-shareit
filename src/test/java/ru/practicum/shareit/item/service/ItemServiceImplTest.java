package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final ItemService itemService;
    private final CommentStorage commentStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final ItemRequestStorage requestStorage;

    private User user;
    private Comment comment;
    private Booking booking;
    private ItemRequest itemRequest;

    @Test
    void create() {
    }

    @Test
    void createComment() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void getById() {
    }

    @Test
    void getAllByUser() {
    }

    @Test
    void search() {
    }
}