package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private ItemRequestService itemRequestService;

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

    @Test
    public void shouldCreateItemRequest() throws Exception {
        requestDto = createRequestDto();
        CreateItemRequestDto createDto = CreateItemRequestDto.builder()
                .description("request")
                .build();
        when(itemRequestService.create(anyLong(), any(LocalDateTime.class), any(CreateItemRequestDto.class)))
                .thenReturn(requestDto);

        response = createItemRequestResponse(1L, createDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(requestDto), response.getContentAsString());

        verify(itemRequestService, times(1))
                .create(anyLong(), any(LocalDateTime.class), any(CreateItemRequestDto.class));
        verifyNoMoreInteractions(itemRequestService);
    }

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
        assertEquals(400, createItemRequestResponse(1L, createDtoBlankDesc).getStatus());

        verifyNoInteractions(itemRequestService);
    }

    private MockHttpServletResponse getItemRequestByIdResponse(Long userId, Long requestId) throws Exception {
        result = mvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @Test
    public void shouldGetItemRequestById() throws Exception {
        requestDto = createRequestDto();
        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        response = getItemRequestByIdResponse(1L, requestDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(requestDto), response.getContentAsString());

        verify(itemRequestService, times(1))
                .getById(anyLong(), anyLong());
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    public void shouldReturn400WhenWrongGetItemRequestById() throws Exception {
        // Проверка валидации userId
        assertEquals(400, getItemRequestByIdResponse(0L, 1L).getStatus());
        assertEquals(400, getItemRequestByIdResponse(-1L, 1L).getStatus());

        // Проверка валидации requestId
        assertEquals(400, getItemRequestByIdResponse(1L, 0L).getStatus());
        assertEquals(400, getItemRequestByIdResponse(1L, -1L).getStatus());

        verifyNoInteractions(itemRequestService);
    }

    private MockHttpServletResponse getItemRequestsByOwnerResponse(Long userId) throws Exception {
        result = mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @Test
    public void shouldGetItemRequestsByOwner() throws Exception {
        requestDto = createRequestDto();
        when(itemRequestService.getByOwnerId(anyLong()))
                .thenReturn(List.of(requestDto));

        response = getItemRequestsByOwnerResponse(1L);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(requestDto)), response.getContentAsString());

        verify(itemRequestService, times(1))
                .getByOwnerId(anyLong());
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    public void shouldReturn400WhenWrongGetItemRequestsByOwner() throws Exception {
        // Проверка валидации userId
        assertEquals(400, getItemRequestsByOwnerResponse(0L).getStatus());
        assertEquals(400, getItemRequestsByOwnerResponse(-1L).getStatus());

        verifyNoInteractions(itemRequestService);
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

    @Test
    public void shouldGetAllItemRequests() throws Exception {
        requestDto = createRequestDto();
        when(itemRequestService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto));

        response = getAllItemRequestsResponse(1L, 0, 5);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(requestDto)), response.getContentAsString());

        verify(itemRequestService, times(1))
                .getAll(anyLong(), anyInt(), anyInt());
        verifyNoMoreInteractions(itemRequestService);
    }

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

        verifyNoInteractions(itemRequestService);
    }
}