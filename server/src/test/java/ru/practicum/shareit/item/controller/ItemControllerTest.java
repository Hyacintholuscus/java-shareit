package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private CreateCommentDto forCreateCommentDto;
    private MockHttpServletResponse response;
    private MvcResult result;

    private ItemDto createItemDto() {
        return ItemDto.builder()
                .id(1L)
                .name("item")
                .description("this is item")
                .available(true)
                .build();
    }

    private CommentDto createCommentDto() {
        return CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("user")
                .created(LocalDateTime.now())
                .build();
    }

    private CreateCommentDto getForCreateCommentDto() {
        return CreateCommentDto.builder()
                .text("text")
                .build();
    }

    private MockHttpServletResponse createItemResponse(Long userId, ItemDto dto) throws Exception {
        result = mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Добавить предмет")
    @Test
    void shouldCreateItem() throws Exception {
        itemDto = createItemDto();
        when(itemService.create(anyLong(), any(ItemDto.class)))
                .thenReturn(itemDto);

        response = createItemResponse(1L, itemDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(itemDto), response.getContentAsString());

        verify(itemService, times(1))
                .create(anyLong(), any(ItemDto.class));
        verifyNoMoreInteractions(itemService);
    }

    private MockHttpServletResponse createCommentResponse(Long itemId,
                                                          Long authorId,
                                                          CreateCommentDto dto) throws Exception {
        result = mvc.perform(post("/items/" + itemId + "/comment")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", authorId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Добавить комментарий")
    @Test
    void shouldCreateComment() throws Exception {
        commentDto = createCommentDto();
        forCreateCommentDto = getForCreateCommentDto();

        when(itemService.createComment(anyLong(),
                anyLong(),
                any(LocalDateTime.class),
                any(CreateCommentDto.class)))
                .thenReturn(commentDto);

        response = createCommentResponse(1L, 1L, forCreateCommentDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(commentDto), response.getContentAsString());

        verify(itemService, times(1))
                .createComment(anyLong(),
                        anyLong(),
                        any(LocalDateTime.class),
                        any(CreateCommentDto.class));

        verifyNoMoreInteractions(itemService);
    }

    private MockHttpServletResponse updateItemResponse(Long itemId,
                                                       Long userId,
                                                       ItemDto dto) throws Exception {
        result = mvc.perform(patch("/items/" + itemId)
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Обновить предмет")
    @Test
    void shouldUpdateItem() throws Exception {
        itemDto = createItemDto();
        ItemDto updatedItem = itemDto.withName("updated")
                .withDescription("updated description")
                .withAvailable(false);

        when(itemService.update(anyLong(), anyLong(), anyMap(), any(LocalDateTime.class)))
                .thenReturn(updatedItem);

        response = updateItemResponse(1L, 1L, updatedItem);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(updatedItem), response.getContentAsString());

        verify(itemService, times(1))
                .update(anyLong(), anyLong(), anyMap(), any(LocalDateTime.class));
        verifyNoMoreInteractions(itemService);
    }

    private MockHttpServletResponse getItemByIdResponse(Long itemId, Long userId) throws Exception {
        result = mvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить предмет по id")
    @Test
    void shouldGetItemById() throws Exception {
        itemDto = createItemDto();
        when(itemService.getById(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(itemDto);

        response = getItemByIdResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(itemDto), response.getContentAsString());

        verify(itemService, times(1))
                .getById(anyLong(), anyLong(), any(LocalDateTime.class));
        verifyNoMoreInteractions(itemService);
    }

    private MockHttpServletResponse getItemsByUserResponse(Long userId,
                                                           int from,
                                                           int size) throws Exception {
        result = mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить предметы по id владельца")
    @Test
    void shouldGetItemsByUser() throws Exception {
        itemDto = createItemDto();
        when(itemService.getAllByUser(anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(itemDto));

        response = getItemsByUserResponse(1L, 0, 5);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(itemDto)), response.getContentAsString());

        verify(itemService, times(1))
                .getAllByUser(anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(itemService);
    }

    private MockHttpServletResponse searchItemsResponse(Long userId, String text, int from, int size) throws Exception {
        result = mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Найти предметы по названию / описанию")
    @Test
    void shouldSearchItems() throws Exception {
        itemDto = createItemDto();
        when(itemService.search(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(List.of(itemDto));

        response = searchItemsResponse(1L, "i", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(itemDto)), response.getContentAsString());

        verify(itemService, times(1))
                .search(anyLong(), anyString(), any(Pageable.class));
        verifyNoMoreInteractions(itemService);
    }

    private MockHttpServletResponse deleteItemResponse(Long userId, Long itemId) throws Exception {
        result = mvc.perform(delete("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Удалить предмет")
    @Test
    void shouldDeleteItem() throws Exception {
        when(itemService.delete(anyLong(), anyLong()))
                .thenReturn(1L);

        response = deleteItemResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(1L), response.getContentAsString());

        verify(itemService, times(1))
                .delete(anyLong(), anyLong());
        verifyNoMoreInteractions(itemService);
    }
}