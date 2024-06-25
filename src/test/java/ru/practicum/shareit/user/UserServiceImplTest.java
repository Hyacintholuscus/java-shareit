package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final UserStorage userStorage;
    private final UserService service;

    private User user;
    private UserDto dto;

    private User createUser() {
        return User.builder()
                .name("user")
                .email("user@email.ru")
                .build();
    }

    private UserDto createUserDto() {
        return UserDto.builder()
                .name("user")
                .email("user@email.ru")
                .build();
    }

    @Test
    public void shouldSaveUpdateGetAndDeleteUser() {
        // Проверка метода getAll() без пользователей в БД
        List<UserDto> actualList = service.getAll();
        assertNotNull(actualList);
        assertTrue(actualList.isEmpty());

        dto = createUserDto();

        // Проверка метода save()
        UserDto savedDto = service.save(dto);
        UserDto expectedSavedDto = dto.withId(savedDto.getId());
        assertNotNull(savedDto);
        assertEquals(expectedSavedDto, savedDto);

        // Проверка метода update()
        UserDto updatedDto = savedDto.withName("updated")
                .withEmail("updated@email.ru");
        UserDto savedUpdatedDto = service.save(updatedDto);
        assertNotNull(savedUpdatedDto);
        assertEquals(updatedDto, savedUpdatedDto);

        // Проверка метода getById()
        UserDto receivedDto = service.getById(savedUpdatedDto.getId());
        assertNotNull(receivedDto);
        assertEquals(savedUpdatedDto, receivedDto);

        // Проверка метода getAll()
        UserDto newDto = UserDto.builder()
                        .name("new user")
                        .email("new@email.ru")
                        .build();
        UserDto savedNewDto = service.save(newDto);
        assertNotNull(savedNewDto);
        assertEquals(newDto.withId(savedNewDto.getId()), savedNewDto);

        List<UserDto> receivedList = service.getAll();
        assertNotNull(receivedList);
        assertEquals(2, receivedList.size());
        assertTrue(receivedList.containsAll(List.of(savedUpdatedDto, savedNewDto)));

        // Проверка метода delete()
        Long deletedId = service.delete(savedUpdatedDto.getId());
        assertNotNull(deletedId);
        assertEquals(savedUpdatedDto.getId(), deletedId);

        Exception exception = assertThrows(NotFoundException.class, () -> {
            service.getById(savedUpdatedDto.getId());
        });
        String expectedMessage = String.format("User with id %d is not exist.", savedUpdatedDto.getId());
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        List<UserDto> listAfterDelete = service.getAll();
        assertEquals(1, listAfterDelete.size());
        assertTrue(receivedList.contains(savedNewDto));
    }

    @Test
    public void shouldThrowWhenCreate() {
        dto = createUserDto();
        UserDto savedDto = service.save(dto);
        UserDto expectedSavedDto = dto.withId(savedDto.getId());
        assertNotNull(savedDto);
        assertEquals(expectedSavedDto, savedDto);

        Exception exception = assertThrows(DuplicateException.class, () -> {
            service.save(dto);
        });
        String expectedMessage = "This email is already in use.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
