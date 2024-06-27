package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final UserService service;

    private UserDto dto;

    private UserDto createUserDto() {
        return UserDto.builder()
                .name("user")
                .email("user@email.ru")
                .build();
    }

    @Test
    public void shouldSaveUpdateGetAndDeleteUser() {
        // Проверка метода getAll() без пользователей в БД
        final List<UserDto> actualList = service.getAll();
        assertNotNull(actualList);
        assertTrue(actualList.isEmpty());

        dto = createUserDto();

        // Проверка метода save()
        final UserDto savedDto = service.save(dto);
        final UserDto expectedSavedDto = dto.withId(savedDto.getId());
        assertNotNull(savedDto);
        assertEquals(expectedSavedDto, savedDto);

        // Проверка метода update()
        final UserDto updatedDto = savedDto.withName("updated")
                .withEmail("updated@email.ru");
        final UserDto savedUpdatedDto = service.save(updatedDto);
        assertNotNull(savedUpdatedDto);
        assertEquals(updatedDto, savedUpdatedDto);

        // Проверка метода getById()
        final UserDto receivedDto = service.getById(savedUpdatedDto.getId());
        assertNotNull(receivedDto);
        assertEquals(savedUpdatedDto, receivedDto);

        // Проверка метода getAll()
        final UserDto newDto = UserDto.builder()
                        .name("new user")
                        .email("new@email.ru")
                        .build();
        final UserDto savedNewDto = service.save(newDto);
        assertNotNull(savedNewDto);
        assertEquals(newDto.withId(savedNewDto.getId()), savedNewDto);

        final List<UserDto> receivedList = service.getAll();
        assertNotNull(receivedList);
        assertEquals(2, receivedList.size());
        assertTrue(receivedList.containsAll(List.of(savedUpdatedDto, savedNewDto)));

        // Проверка метода delete()
        final Long deletedId = service.delete(savedUpdatedDto.getId());
        assertNotNull(deletedId);
        assertEquals(savedUpdatedDto.getId(), deletedId);

        final Exception exception = assertThrows(NotFoundException.class, () -> {
            service.getById(savedUpdatedDto.getId());
        });
        final String expectedMessage = String.format("User with id %d is not exist.", savedUpdatedDto.getId());
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

        final List<UserDto> listAfterDelete = service.getAll();
        assertEquals(1, listAfterDelete.size());
        assertTrue(receivedList.contains(savedNewDto));
    }

    @Test
    public void shouldThrowWhenCreate() {
        dto = createUserDto();
        final UserDto savedDto = service.save(dto);
        final UserDto expectedSavedDto = dto.withId(savedDto.getId());
        assertNotNull(savedDto);
        assertEquals(expectedSavedDto, savedDto);

        final Exception exception = assertThrows(DuplicateException.class, () -> {
            service.save(dto);
        });
        final String expectedMessage = "This email is already in use.";
        final String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
