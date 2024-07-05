package ru.practicum.shareit.request.controller;

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

    @DisplayName("Добавить запрос")
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
        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(requestDto);

        response = getItemRequestByIdResponse(1L, requestDto.getId());

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(requestDto), response.getContentAsString());

        verify(itemRequestService, times(1))
                .getById(anyLong(), anyLong());
        verifyNoMoreInteractions(itemRequestService);
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
        when(itemRequestService.getByOwnerId(anyLong()))
                .thenReturn(List.of(requestDto));

        response = getItemRequestsByOwnerResponse(1L);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(requestDto)), response.getContentAsString());

        verify(itemRequestService, times(1))
                .getByOwnerId(anyLong());
        verifyNoMoreInteractions(itemRequestService);
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
        when(itemRequestService.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(requestDto));

        response = getAllItemRequestsResponse(1L, 0, 5);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(requestDto)), response.getContentAsString());

        verify(itemRequestService, times(1))
                .getAll(anyLong(), anyInt(), anyInt());
        verifyNoMoreInteractions(itemRequestService);
    }
}