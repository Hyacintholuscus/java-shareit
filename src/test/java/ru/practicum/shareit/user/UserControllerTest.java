package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @Test
    public void shouldReturn400WhenWrongCreateUser() throws Exception {
        // Проверка валидации имени
        userDto = UserDto.builder()
                .name("")
                .email("user@email.ru")
                .build();
        assertEquals(400, createUserResponse(userDto).getStatus());

        UserDto dtoNullName = UserDto.builder()
                .name(null)
                .email("user@email.ru")
                .build();
        assertEquals(400, createUserResponse(dtoNullName).getStatus());

        // Проверка валидации email
        UserDto dtoNullEmail = UserDto.builder()
                .name("user")
                .email(null)
                .build();
        assertEquals(400, createUserResponse(dtoNullEmail).getStatus());

        UserDto dtoWrongEmail = UserDto.builder()
                .name("user")
                .email("email")
                .build();
        assertEquals(400, createUserResponse(dtoWrongEmail).getStatus());

        verifyNoInteractions(userService);
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

    @Test
    public void shouldUpdateUser() throws Exception {
        userDto = createDto();
        UserDto expectedUpdatedDto = UserDto.builder()
                .id(userDto.getId())
                .name("updated")
                .email("updated@email")
                .build();

        when(userService.getById(anyLong()))
                .thenReturn(userDto);
        when(userService.save(any(UserDto.class)))
                .thenReturn(expectedUpdatedDto);

        response = updateUserResponse(userDto.getId(), expectedUpdatedDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(expectedUpdatedDto), response.getContentAsString());

        verify(userService, times(1)).getById(anyLong());
        verify(userService, times(1)).save(any(UserDto.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void shouldReturn400WhenWrongUpdateUser() throws Exception {
        userDto = createDto();

        assertEquals(400, updateUserResponse(0L, userDto).getStatus());
        assertEquals(400, updateUserResponse(-1L, userDto).getStatus());

        verifyNoInteractions(userService);
    }

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

    @Test
    public void shouldReturn400WhenWrongGetUserById() throws Exception {
        assertEquals(400, getUserResponse(0L).getStatus());
        assertEquals(400, getUserResponse(-1L).getStatus());

        verifyNoInteractions(userService);
    }

    private MockHttpServletResponse deleteUserResponse(Long id) throws Exception {
        result = mvc.perform(delete("/users/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

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

    @Test
    public void shouldReturn400WhenWrongDeleteUser() throws Exception {
        assertEquals(400, deleteUserResponse(0L).getStatus());
        assertEquals(400, deleteUserResponse(-1L).getStatus());

        verifyNoInteractions(userService);
    }
}
