package ru.practicum.shareitgate.item;

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
import ru.practicum.shareitgate.item.dto.CommentDto;
import ru.practicum.shareitgate.item.dto.CreateCommentDto;
import ru.practicum.shareitgate.item.dto.CreateItemDto;
import ru.practicum.shareitgate.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private ItemClient itemClient;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private CreateItemDto creationItemDto;
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

    private CreateItemDto getCreationItemDto() {
        return CreateItemDto.builder()
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

    private MockHttpServletResponse createItemResponse(Long userId, CreateItemDto dto) throws Exception {
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
        creationItemDto = getCreationItemDto();
        when(itemClient.createItem(anyLong(), any(CreateItemDto.class)))
                .thenReturn(ResponseEntity.ok(itemDto));

        response = createItemResponse(1L, creationItemDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(itemDto), response.getContentAsString());

        verify(itemClient, times(1))
                .createItem(anyLong(), any(CreateItemDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при добавлении предмета")
    @Test
    public void shouldReturn400WhenWrongCreateItem() throws Exception {
        // Проверка валидации userId
        creationItemDto = getCreationItemDto();
        assertEquals(400, createItemResponse(0L, creationItemDto).getStatus());
        assertEquals(400, createItemResponse(-1L, creationItemDto).getStatus());

        //Проверка валидации имени
        final CreateItemDto dtoNullName = creationItemDto.withName(null);
        assertEquals(400, createItemResponse(1L, dtoNullName).getStatus());

        final CreateItemDto dtoBlankName = creationItemDto.withName("");
        assertEquals(400, createItemResponse(1L, dtoBlankName).getStatus());

        final CreateItemDto dto61CharName = creationItemDto.withName("i".repeat(61));
        assertEquals(400, createItemResponse(1L, dto61CharName).getStatus());

        // Проверка валидации описания
        final CreateItemDto dtoNullDesc = creationItemDto.withDescription(null);
        assertEquals(400, createItemResponse(1L, dtoNullDesc).getStatus());

        final CreateItemDto dtoBlankDesc = creationItemDto.withDescription("");
        assertEquals(400, createItemResponse(1L, dtoBlankDesc).getStatus());

        final CreateItemDto dto201CharDesc = creationItemDto.withDescription("i".repeat(201));
        assertEquals(400, createItemResponse(1L, dto201CharDesc).getStatus());

        // Проверка валидации статуса
        final CreateItemDto dtoNullAvail = creationItemDto.withAvailable(null);
        assertEquals(400, createItemResponse(1L, dtoNullAvail).getStatus());

        verifyNoInteractions(itemClient);
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

        when(itemClient.createComment(anyLong(), anyLong(), any(CreateCommentDto.class)))
                .thenReturn(ResponseEntity.ok(commentDto));

        response = createCommentResponse(1L, 1L, forCreateCommentDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(commentDto), response.getContentAsString());

        verify(itemClient, times(1))
                .createComment(anyLong(), anyLong(), any(CreateCommentDto.class));
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при добавлении комментария")
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

        verifyNoInteractions(itemClient);
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

        when(itemClient.updateItem(anyLong(), anyLong(), anyMap()))
                .thenReturn(ResponseEntity.ok(updatedItem));

        response = updateItemResponse(1L, 1L, updatedItem);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(updatedItem), response.getContentAsString());

        verify(itemClient, times(1))
                .updateItem(anyLong(), anyLong(), anyMap());
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при обновлении предмета")
    @Test
    public void shouldReturn400WhenWrongUpdateItem() throws Exception {
        // Проверка валидации itemId
        itemDto = createItemDto();
        assertEquals(400, updateItemResponse(0L, 1L, itemDto).getStatus());
        assertEquals(400, updateItemResponse(-1L, 1L, itemDto).getStatus());

        // Проверка валидации userId
        assertEquals(400, updateItemResponse(1L, 0L, itemDto).getStatus());
        assertEquals(400, updateItemResponse(1L, -1L, itemDto).getStatus());

        verifyNoInteractions(itemClient);
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
        when(itemClient.getItemById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(itemDto));

        response = getItemByIdResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(itemDto), response.getContentAsString());

        verify(itemClient, times(1))
                .getItemById(anyLong(), anyLong());
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при получении предмета по id")
    @Test
    public void shouldReturn400WhenWrongGetItemById() throws Exception {
        // Проверка валидации itemId
        assertEquals(400, getItemByIdResponse(0L, 1L).getStatus());
        assertEquals(400, getItemByIdResponse(-1L, 1L).getStatus());

        // Проверка валидации userId
        assertEquals(400, getItemByIdResponse(1L, 0L).getStatus());
        assertEquals(400, getItemByIdResponse(1L, -1L).getStatus());

        verifyNoInteractions(itemClient);
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
        when(itemClient.getItemsByUser(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));

        response = getItemsByUserResponse(1L, 0, 5);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(itemDto)), response.getContentAsString());

        verify(itemClient, times(1))
                .getItemsByUser(anyLong(), anyInt(), anyInt());
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при получении предметов по id владельца")
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

        verifyNoInteractions(itemClient);
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
        when(itemClient.searchItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of(itemDto)));

        response = searchItemsResponse(1L, "i", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(itemDto)), response.getContentAsString());

        final MockHttpServletResponse emptyListResp = searchItemsResponse(1L, "", 0, 5);
        assertEquals(200, emptyListResp.getStatus());
        assertEquals(mapper.writeValueAsString(new ArrayList<>()), emptyListResp.getContentAsString());

        verify(itemClient, times(1))
                .searchItems(anyLong(), anyString(), anyInt(), anyInt());
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при поиске предметов по названию / описанию")
    @Test
    public void shouldReturn400WhenWrongSearchItems() throws Exception {
        // Проверка валидации userId
        assertEquals(400, searchItemsResponse(0L, "i", 0, 5).getStatus());
        assertEquals(400, searchItemsResponse(-1L, "i", 0, 5).getStatus());

        // Проверка валидации from
        assertEquals(400, searchItemsResponse(1L, "i", -1, 5).getStatus());

        // Проверка валидации size
        assertEquals(400, searchItemsResponse(1L, "i", 0, 0).getStatus());
        assertEquals(400, searchItemsResponse(1L, "i", 0, -1).getStatus());

        verifyNoInteractions(itemClient);
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
        when(itemClient.deleteItem(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(1L));

        response = deleteItemResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(1L), response.getContentAsString());

        verify(itemClient, times(1))
                .deleteItem(anyLong(), anyLong());
        verifyNoMoreInteractions(itemClient);
    }

    @DisplayName("Статус 400 при удалении предмета")
    @Test
    public void shouldReturn400WhenWrongDeleteItem() throws Exception {
        // Проверка валидации userId
        assertEquals(400, deleteItemResponse(0L, 1L).getStatus());
        assertEquals(400, deleteItemResponse(-1L, 1L).getStatus());

        // Проверка валидации itemId
        assertEquals(400, deleteItemResponse(1L, 0L).getStatus());
        assertEquals(400, deleteItemResponse(1L, -1L).getStatus());

        verifyNoInteractions(itemClient);
    }
}