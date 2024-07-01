package ru.practicum.shareitgate.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareitgate.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private UserClient userClient;

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
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(userDto));

        response = createUserResponse(userDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(userDto), response.getContentAsString());

        verify(userClient, times(1))
                .createUser(any(UserDto.class));
        verifyNoMoreInteractions(userClient);
    }

    @DisplayName("Статус 400 при добавлении пользователя")
    @Test
    public void shouldReturn400WhenWrongCreateUser() throws Exception {
        // Проверка валидации имени
        userDto = createDto();
        UserDto dtoBlankName = userDto.withName("");
        assertEquals(400, createUserResponse(dtoBlankName).getStatus());

        UserDto dtoNullName = userDto.withName(null);
        assertEquals(400, createUserResponse(dtoNullName).getStatus());

        UserDto dto201CharName = userDto.withName("u".repeat(201));
        assertEquals(400, createUserResponse(dto201CharName).getStatus());

        // Проверка валидации email
        UserDto dtoNullEmail = userDto.withEmail(null);
        assertEquals(400, createUserResponse(dtoNullEmail).getStatus());

        UserDto dtoWrongEmail = userDto.withEmail("email");
        assertEquals(400, createUserResponse(dtoWrongEmail).getStatus());

        UserDto dto201CharEmail = userDto.withEmail("e".repeat(201));
        assertEquals(400, createUserResponse(dto201CharEmail).getStatus());

        verifyNoInteractions(userClient);
    }

    private MockHttpServletResponse updateUserResponse(Long id, UserDto userDto) throws Exception {
        result = mvc.perform(patch("/users/" + id)
                        .content(mapper.writeValueAsString(userDto))
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

        when(userClient.updateUser(anyLong(), anyMap()))
                .thenReturn(ResponseEntity.ok(expectedUpdatedDto));

        response = updateUserResponse(userDto.getId(), expectedUpdatedDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(expectedUpdatedDto), response.getContentAsString());

        verify(userClient, times(1)).updateUser(anyLong(), anyMap());
        verifyNoMoreInteractions(userClient);
    }

    @DisplayName("Статус 400 при обновлении пользователя")
    @Test
    public void shouldReturn400WhenWrongUpdateUser() throws Exception {
        userDto = createDto();

        assertEquals(400, updateUserResponse(0L, userDto).getStatus());
        assertEquals(400, updateUserResponse(-1L, userDto).getStatus());

        verifyNoInteractions(userClient);
    }

    @DisplayName("Получить всех пользователей")
    @Test
    public void shouldGetAllUsers() throws Exception {
        userDto = createDto();
        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(List.of(userDto)));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().json(mapper.writeValueAsString(List.of(userDto))));

        verify(userClient, times(1)).getAllUsers();
        verifyNoMoreInteractions(userClient);
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
        when(userClient.getUserById(anyLong()))
                .thenReturn(ResponseEntity.ok(userDto));

        response = getUserResponse(userDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(userDto), response.getContentAsString());

        verify(userClient, times(1)).getUserById(anyLong());
        verifyNoMoreInteractions(userClient);
    }

    @DisplayName("Статус 400 при получении пользователя по id")
    @Test
    public void shouldReturn400WhenWrongGetUserById() throws Exception {
        assertEquals(400, getUserResponse(0L).getStatus());
        assertEquals(400, getUserResponse(-1L).getStatus());

        verifyNoInteractions(userClient);
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
        when(userClient.deleteUserById(anyLong()))
                .thenReturn(ResponseEntity.ok(userDto.getId()));

        response = deleteUserResponse(userDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(userDto.getId()), response.getContentAsString());

        verify(userClient, times(1)).deleteUserById(anyLong());
        verifyNoMoreInteractions(userClient);
    }

    @DisplayName("Статус 400 при удалении пользователя")
    @Test
    public void shouldReturn400WhenWrongDeleteUser() throws Exception {
        assertEquals(400, deleteUserResponse(0L).getStatus());
        assertEquals(400, deleteUserResponse(-1L).getStatus());

        verifyNoInteractions(userClient);
    }
}