package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Test
    public void shouldReturn400WhenWrongCreateItem() throws Exception {
        // Проверка валидации userId
        itemDto = createItemDto();
        assertEquals(400, createItemResponse(0L, itemDto).getStatus());
        assertEquals(400, createItemResponse(-1L, itemDto).getStatus());

        //Проверка валидации имени
        ItemDto dtoNullName = itemDto.withName(null);
        assertEquals(400, createItemResponse(1L, dtoNullName).getStatus());

        ItemDto dtoBlankName = itemDto.withName("");
        assertEquals(400, createItemResponse(1L, dtoBlankName).getStatus());

        ItemDto dto61CharName = itemDto.withName("i".repeat(61));
        assertEquals(400, createItemResponse(1L, dto61CharName).getStatus());

        // Проверка валидации описания
        ItemDto dtoNullDesc = itemDto.withDescription(null);
        assertEquals(400, createItemResponse(1L, dtoNullDesc).getStatus());

        ItemDto dtoBlankDesc = itemDto.withDescription("");
        assertEquals(400, createItemResponse(1L, dtoBlankDesc).getStatus());

        ItemDto dto201CharDesc = itemDto.withDescription("i".repeat(201));
        assertEquals(400, createItemResponse(1L, dto201CharDesc).getStatus());

        // Проверка валидации статуса
        ItemDto dtoNullAvail = itemDto.withAvailable(null);
        assertEquals(400, createItemResponse(1L, dtoNullAvail).getStatus());

        verifyNoInteractions(itemService);
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

    @Test
    public void shouldReturn400WhenWrongCreateComment() throws Exception {
        // Проверка валидации itemId
        forCreateCommentDto = getForCreateCommentDto();
        assertEquals(400, createCommentResponse(0L, 1L, forCreateCommentDto).getStatus());
        assertEquals(400, createCommentResponse(-1L, 1L, forCreateCommentDto).getStatus());

        //Проверка валидации authorId
        assertEquals(400, createCommentResponse(1L, 0L, forCreateCommentDto).getStatus());
        assertEquals(400, createCommentResponse(1L, -1L, forCreateCommentDto).getStatus());

        //Проверка валидации текста
        CreateCommentDto dtoNullText = forCreateCommentDto.withText(null);
        assertEquals(400, createCommentResponse(1L, 1L, dtoNullText).getStatus());

        CreateCommentDto dtoBlankText = forCreateCommentDto.withText("");
        assertEquals(400, createCommentResponse(1L, 1L, dtoBlankText).getStatus());

        CreateCommentDto dto1001CharText = forCreateCommentDto.withText("c".repeat(1001));
        assertEquals(400, createCommentResponse(1L, 1L, dto1001CharText).getStatus());

        verifyNoInteractions(itemService);
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

    @Test
    public void shouldReturn400WhenWrongUpdateItem() throws Exception {
        // Проверка валидации itemId
        itemDto = createItemDto();
        assertEquals(400, updateItemResponse(0L, 1L, itemDto).getStatus());
        assertEquals(400, updateItemResponse(-1L, 1L, itemDto).getStatus());

        // Проверка валидации userId
        assertEquals(400, updateItemResponse(1L, 0L, itemDto).getStatus());
        assertEquals(400, updateItemResponse(1L, -1L, itemDto).getStatus());

        verifyNoInteractions(itemService);
    }

    private MockHttpServletResponse getItemByIdResponse(Long itemId, Long userId) throws Exception {
        result = mvc.perform(get("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

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

    @Test
    public void shouldReturn400WhenWrongGetItemById() throws Exception {
        // Проверка валидации itemId
        assertEquals(400, getItemByIdResponse(0L, 1L).getStatus());
        assertEquals(400, getItemByIdResponse(-1L, 1L).getStatus());

        // Проверка валидации userId
        assertEquals(400, getItemByIdResponse(1L, 0L).getStatus());
        assertEquals(400, getItemByIdResponse(1L, -1L).getStatus());

        verifyNoInteractions(itemService);
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

    @Test
    public void shouldReturn400WhenWrongGetItemsByUser() throws Exception {
        // Проверка валидации userId
        assertEquals(400, getItemsByUserResponse(0L, 0, 5).getStatus());
        assertEquals(400, getItemsByUserResponse(-1L, 0, 5).getStatus());

        // Проверка валидации from
        assertEquals(400, getItemsByUserResponse(1L, -1, 5).getStatus());

        // Проверка валидации size
        assertEquals(400, getItemsByUserResponse(1L, 0, 0).getStatus());
        assertEquals(400, getItemsByUserResponse(1L, 0, -1).getStatus());

        verifyNoInteractions(itemService);
    }

    private MockHttpServletResponse searchItemsResponse(String text, int from, int size) throws Exception {
        result = mvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @Test
    void shouldSearchItems() throws Exception {
        itemDto = createItemDto();
        when(itemService.search(anyString(), any(Pageable.class)))
                .thenReturn(List.of(itemDto));

        response = searchItemsResponse("i", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(itemDto)), response.getContentAsString());

        final MockHttpServletResponse emptyListResp = searchItemsResponse("", 0, 5);
        assertEquals(200, emptyListResp.getStatus());
        assertEquals(mapper.writeValueAsString(new ArrayList<>()), emptyListResp.getContentAsString());

        verify(itemService, times(1))
                .search(anyString(), any(Pageable.class));
        verifyNoMoreInteractions(itemService);
    }

    @Test
    public void shouldReturn400WhenWrongSearchItems() throws Exception {
        // Проверка валидации from
        assertEquals(400, searchItemsResponse("i", -1, 5).getStatus());

        // Проверка валидации size
        assertEquals(400, searchItemsResponse("i", 0, 0).getStatus());
        assertEquals(400, searchItemsResponse("i", 0, -1).getStatus());

        verifyNoInteractions(itemService);
    }

    private MockHttpServletResponse deleteItemResponse(Long userId, Long itemId) throws Exception {
        result = mvc.perform(delete("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

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

    @Test
    public void shouldReturn400WhenWrongDeleteItem() throws Exception {
        // Проверка валидации userId
        assertEquals(400, deleteItemResponse(0L, 1L).getStatus());
        assertEquals(400, deleteItemResponse(-1L, 1L).getStatus());

        // Проверка валидации itemId
        assertEquals(400, deleteItemResponse(1L, 0L).getStatus());
        assertEquals(400, deleteItemResponse(1L, -1L).getStatus());

        verifyNoInteractions(itemService);
    }
}