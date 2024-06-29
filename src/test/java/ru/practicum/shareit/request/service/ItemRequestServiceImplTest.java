package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    private final ItemRequestService itemRequestService;
    private final UserStorage userStorage;

    private User user;
    private ItemRequestDto requestDto;
    private CreateItemRequestDto creationRequestDto;

    @BeforeEach
    void beforeEach() {
        user = userStorage.save(User.builder()
                .name("user")
                .email("user@email.ru")
                .build());
        creationRequestDto = CreateItemRequestDto.builder()
                .description("new description")
                .build();
    }

    @DisplayName("Добавить запрос")
    @Test
    public void shouldCreateItemRequest() {
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        requestDto = itemRequestService.create(user.getId(), creationTime, creationRequestDto);

        assertNotNull(requestDto);
        assertNotNull(requestDto.getId());
        assertEquals(creationRequestDto.getDescription(), requestDto.getDescription());
        assertEquals(creationTime, requestDto.getCreated());
    }

    @DisplayName("Исключение при добавлении запроса")
    @Test
    public void shouldThrowWhenCreateItemRequest() {
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final Long userId = 9999L;
        final Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.create(userId, creationTime, creationRequestDto);
        });
        final String expectedMessage = String.format("User with id %d is not exist.", userId);
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @DisplayName("Получить запрос по id")
    @Test
    public void shouldGetByIdItemRequest() {
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        requestDto = itemRequestService.create(user.getId(), creationTime, creationRequestDto);

        final ItemRequestDto receivedDto = itemRequestService.getById(user.getId(), requestDto.getId());
        assertNotNull(receivedDto);
        assertEquals(requestDto, receivedDto);
    }

    @DisplayName("Исключения при получении запроса по id")
    @Test
    public void shouldThrowWhenGetByIdItemRequest() {
        // Проверка проброса исключения NotFoundException с несуществующим userId
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        requestDto = itemRequestService.create(user.getId(), creationTime, creationRequestDto);
        final Long userId = 9999L;
        final Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getById(userId, requestDto.getId());
        });
        final String expectedMessage = String.format("User with id %d is not exist.", userId);
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        // Проверка проброса исключения NotFoundException с несуществующим requestId
        final Long requestId = 9999L;
        final Exception exc = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getById(user.getId(), requestId);
        });
        final String expectedMess = String.format("ItemRequest with id %d is not exist.", requestId);
        final String actualMess = exc.getMessage();
        assertTrue(actualMess.contains(expectedMess));
    }

    @DisplayName("Получить запросы по id владельца")
    @Test
    public void shouldGetByOwnerIdItemRequest() {
        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        requestDto = itemRequestService.create(user.getId(), creationTime, creationRequestDto);

        // Проверка получения списка запросов
        final List<ItemRequestDto> receivedList = itemRequestService.getByOwnerId(user.getId());
        assertNotNull(receivedList);
        assertEquals(1, receivedList.size());
        assertTrue(receivedList.contains(requestDto));

        // Проверка с пустым листом
        User newUser = userStorage.save(User.builder()
                .name("new user")
                .email("newuser@email.ru")
                .build());
        assertNotNull(user);
        final List<ItemRequestDto> listNewUser = itemRequestService.getByOwnerId(newUser.getId());
        assertNotNull(listNewUser);
        assertTrue(listNewUser.isEmpty());
    }

    @DisplayName("Исключение при получении запросов по id владельца")
    @Test
    public void shouldThrowWhenGetByOwnerIdItemRequest() {
        // Проверка проброса исключения NotFoundException с несуществующим userId
        final Long userId = 9999L;
        final Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getByOwnerId(userId);
        });
        final String expectedMessage = String.format("User with id %d is not exist.", userId);
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @DisplayName("Получить все запросы от других пользователей")
    @Test
    void shouldGetAllRequest() {
        // Проверка метода getAll() без ItemRequest в БД
        final List<ItemRequestDto> actualList = itemRequestService.getAll(user.getId(), 0, 5);
        assertNotNull(actualList);
        assertTrue(actualList.isEmpty());

        final LocalDateTime creationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        requestDto = itemRequestService.create(user.getId(), creationTime, creationRequestDto);

        final LocalDateTime secondCreationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final ItemRequestDto secondDto = itemRequestService.create(user.getId(), secondCreationTime, creationRequestDto);

        User newUser = userStorage.save(User.builder()
                .name("new user")
                .email("newuser@email.ru")
                .build());
        assertNotNull(user);

        final LocalDateTime thirdCreationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final ItemRequestDto thirdDto = itemRequestService.create(newUser.getId(), thirdCreationTime, creationRequestDto);

        // Проверка метода getAll() для 1-го пользователя
        final List<ItemRequestDto> listWithAllForUser = itemRequestService.getAll(user.getId(), 0, 5);
        assertNotNull(listWithAllForUser);
        assertEquals(1, listWithAllForUser.size());
        assertTrue(listWithAllForUser.contains(thirdDto));

        // Проверка метода getAll() для 2-го пользователя
        final List<ItemRequestDto> listWithAllForNewUser = itemRequestService.getAll(newUser.getId(), 0, 5);
        assertNotNull(listWithAllForNewUser);
        assertEquals(2, listWithAllForNewUser.size());
        assertTrue(listWithAllForNewUser.containsAll(List.of(requestDto, secondDto)));

        // Проверка метода getAll() с другими параметрами from и size

        final LocalDateTime fourthCreationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final ItemRequestDto fourthDto = itemRequestService.create(user.getId(), fourthCreationTime, creationRequestDto);

        final LocalDateTime fifthCreationTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        final ItemRequestDto fifthDto = itemRequestService.create(user.getId(), fifthCreationTime, creationRequestDto);

        final List<ItemRequestDto> listRequestsSize2 = itemRequestService.getAll(newUser.getId(), 1, 2);
        assertNotNull(listRequestsSize2);
        assertEquals(2, listRequestsSize2.size());
        assertTrue(listRequestsSize2.containsAll(List.of(fifthDto, fourthDto)));

        final List<ItemRequestDto> listWithRequestsSize3 = itemRequestService.getAll(newUser.getId(), 3, 3);
        assertNotNull(listWithRequestsSize3);
        assertEquals(1, listWithRequestsSize3.size());
        assertTrue(listWithRequestsSize3.contains(requestDto));
    }

    @DisplayName("Исключение при получении всех запросов от других пользователей")
    @Test
    public void shouldThrowWhenGetAllItemRequest() {
        // Проверка проброса исключения NotFoundException с несуществующим userId
        final Long userId = 9999L;
        final Exception exception = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getAll(userId, 0, 5);
        });
        final String expectedMessage = String.format("User with id %d is not exist.", userId);
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
