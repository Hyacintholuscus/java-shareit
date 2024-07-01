package ru.practicum.shareitgate.request;

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
import ru.practicum.shareitgate.request.dto.CreateItemRequestDto;
import ru.practicum.shareitgate.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestDto requestDto;
    private MockHttpServletResponse response;
    private MvcResult result;

    private ItemRequestDto createRequestDto() {
        return ItemRequestDto.builder()
                .id(1L)
                .description("request")
                .created(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }

    private MockHttpServletResponse createItemRequestResponse(Long userId,
                                                              CreateItemRequestDto dto) throws Exception {
        result = mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Добавить запрос")
    @Test
    public void shouldCreateItemRequest() throws Exception {
        requestDto = createRequestDto();
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("request")
                .build();
        when(itemRequestClient.createRequest(anyLong(), any(CreateItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(requestDto));

        response = createItemRequestResponse(1L, createDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(requestDto), response.getContentAsString());

        verify(itemRequestClient, times(1))
                .createRequest(anyLong(), any(CreateItemRequestDto.class));
        verifyNoMoreInteractions(itemRequestClient);
    }

    @DisplayName("Статус 400 при добавлении запроса")
    @Test
    public void shouldReturn400WhenWrongCreateItemRequest() throws Exception {
        // Проверка валидации userId
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("request")
                .build();
        assertEquals(400, createItemRequestResponse(0L, createDto).getStatus());
        assertEquals(400, createItemRequestResponse(-1L, createDto).getStatus());

        // Проверка валидации описания
        CreateItemRequestDto createDtoNullDesc = createDto.withDescription(null);
        assertEquals(400, createItemRequestResponse(1L, createDtoNullDesc).getStatus());

        CreateItemRequestDto createDtoBlankDesc = createDto.withDescription("");
        assertEquals(400, createItemRequestResponse(1L, createDtoBlankDesc).getStatus());

        CreateItemRequestDto createDto301CharDesc = createDto.withDescription("r".repeat(301));
        assertEquals(400, createItemRequestResponse(1L, createDto301CharDesc).getStatus());

        verifyNoInteractions(itemRequestClient);
    }

    private MockHttpServletResponse getItemRequestByIdResponse(Long userId, Long requestId) throws Exception {
        result = mvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить запрос по id")
    @Test
    public void shouldGetItemRequestById() throws Exception {
        requestDto = createRequestDto();
        when(itemRequestClient.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(requestDto));

        response = getItemRequestByIdResponse(1L, requestDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(requestDto), response.getContentAsString());

        verify(itemRequestClient, times(1))
                .getById(anyLong(), anyLong());
        verifyNoMoreInteractions(itemRequestClient);
    }

    @DisplayName("Статус 400 при получении запроса по id")
    @Test
    public void shouldReturn400WhenWrongGetItemRequestById() throws Exception {
        // Проверка валидации userId
        assertEquals(400, getItemRequestByIdResponse(0L, 1L).getStatus());
        assertEquals(400, getItemRequestByIdResponse(-1L, 1L).getStatus());

        // Проверка валидации requestId
        assertEquals(400, getItemRequestByIdResponse(1L, 0L).getStatus());
        assertEquals(400, getItemRequestByIdResponse(1L, -1L).getStatus());

        verifyNoInteractions(itemRequestClient);
    }

    private MockHttpServletResponse getItemRequestsByOwnerResponse(Long userId) throws Exception {
        result = mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить запросы по id владельца")
    @Test
    public void shouldGetItemRequestsByOwner() throws Exception {
        requestDto = createRequestDto();
        when(itemRequestClient.getByOwner(anyLong()))
                .thenReturn(ResponseEntity.ok(List.of(requestDto)));

        response = getItemRequestsByOwnerResponse(1L);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(requestDto)), response.getContentAsString());

        verify(itemRequestClient, times(1))
                .getByOwner(anyLong());
        verifyNoMoreInteractions(itemRequestClient);
    }

    @DisplayName("Статус 400 при получении запросов по id владельца")
    @Test
    public void shouldReturn400WhenWrongGetItemRequestsByOwner() throws Exception {
        // Проверка валидации userId
        assertEquals(400, getItemRequestsByOwnerResponse(0L).getStatus());
        assertEquals(400, getItemRequestsByOwnerResponse(-1L).getStatus());

        verifyNoInteractions(itemRequestClient);
    }

    private MockHttpServletResponse getAllItemRequestsResponse(Long userId, int from, int size) throws Exception {
        result = mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить все запросы от других пользователей")
    @Test
    public void shouldGetAllItemRequests() throws Exception {
        requestDto = createRequestDto();
        when(itemRequestClient.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of(requestDto)));

        response = getAllItemRequestsResponse(1L, 0, 5);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(requestDto)), response.getContentAsString());

        verify(itemRequestClient, times(1))
                .getAll(anyLong(), anyInt(), anyInt());
        verifyNoMoreInteractions(itemRequestClient);
    }

    @DisplayName("Статус 400 при получении всех запросов от других пользователей")
    @Test
    public void shouldReturn400WhenWrongGetAllItemRequests() throws Exception {
        // Проверка валидации userId
        assertEquals(400, getAllItemRequestsResponse(0L, 0, 5).getStatus());
        assertEquals(400, getAllItemRequestsResponse(-1L, 0, 5).getStatus());

        // Проверка валидации from
        assertEquals(400, getAllItemRequestsResponse(1L, -1, 5).getStatus());

        // Проверка валидации size
        assertEquals(400, getAllItemRequestsResponse(1L, 0, 0).getStatus());
        assertEquals(400, getAllItemRequestsResponse(1L, 0, -1).getStatus());

        verifyNoInteractions(itemRequestClient);
    }
}