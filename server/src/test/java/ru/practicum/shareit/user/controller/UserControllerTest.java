package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private UserService userService;

    private UserDto userDto;
    private MockHttpServletResponse response;
    private MvcResult result;

    private UserDto createDto() {
        return UserDto.builder()
                .id(1L)
                .name("user")
                .email("user@email.ru")
                .build();
    }

    private MockHttpServletResponse createUserResponse(UserDto userDto) throws Exception {
        result = mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Добавить пользователя")
    @Test
    public void shouldCreateUser() throws Exception {
        userDto = createDto();
        when(userService.save(any(UserDto.class)))
                .thenReturn(userDto);

        response = createUserResponse(userDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(userDto), response.getContentAsString());

        verify(userService, times(1))
                .save(any(UserDto.class));
        verifyNoMoreInteractions(userService);
    }

    private MockHttpServletResponse updateUserResponse(Long id, Map<String, Object> updatedFields) throws Exception {
        result = mvc.perform(patch("/users/" + id)
                        .content(mapper.writeValueAsString(updatedFields))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Обновить пользователя")
    @Test
    public void shouldUpdateUser() throws Exception {
        userDto = createDto();
        final UserDto expectedUpdatedDto = userDto
                .withName("updated")
                .withEmail("updated@email");
        final Map<String, Object> updatedFields = Map.of(
                "name", "updated",
                "email", "updated@email"
        );

        when(userService.getById(anyLong()))
                .thenReturn(userDto);
        when(userService.save(any(UserDto.class)))
                .thenReturn(expectedUpdatedDto);

        response = updateUserResponse(userDto.getId(), updatedFields);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(expectedUpdatedDto), response.getContentAsString());

        verify(userService, times(1)).getById(anyLong());
        verify(userService, times(1)).save(any(UserDto.class));
        verifyNoMoreInteractions(userService);
    }

    @DisplayName("Получить всех пользователей")
    @Test
    public void shouldGetAllUsers() throws Exception {
        userDto = createDto();
        when(userService.getAll())
                .thenReturn(List.of(userDto));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(List.of(userDto))));

        verify(userService, times(1)).getAll();
        verifyNoMoreInteractions(userService);
    }

    private MockHttpServletResponse getUserResponse(Long id) throws Exception {
        result = mvc.perform(get("/users/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить пользователя по id")
    @Test
    public void shouldGetUserById() throws Exception {
        userDto = createDto();
        when(userService.getById(anyLong()))
                .thenReturn(userDto);

        response = getUserResponse(userDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(userDto), response.getContentAsString());

        verify(userService, times(1)).getById(anyLong());
        verifyNoMoreInteractions(userService);
    }

    private MockHttpServletResponse deleteUserResponse(Long id) throws Exception {
        result = mvc.perform(delete("/users/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Удалить пользователя")
    @Test
    public void shouldDeleteUser() throws Exception {
        userDto = createDto();
        when(userService.delete(anyLong()))
                .thenReturn(userDto.getId());

        response = deleteUserResponse(userDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(userDto.getId()), response.getContentAsString());

        verify(userService, times(1)).delete(anyLong());
        verifyNoMoreInteractions(userService);
    }
}