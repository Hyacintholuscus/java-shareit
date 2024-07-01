package ru.practicum.shareitgate.booking;

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
import ru.practicum.shareitgate.booking.dto.BookingDto;
import ru.practicum.shareitgate.booking.dto.BookingState;
import ru.practicum.shareitgate.booking.dto.BookingStatus;
import ru.practicum.shareitgate.booking.dto.CreateBookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private BookingClient bookingClient;

    private BookingDto bookingDto;
    private CreateBookingDto creationDto;
    private MockHttpServletResponse response;
    private MvcResult result;

    private BookingDto createBookingDto() {
        return BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(5))
                .end(LocalDateTime.now().plusMinutes(10))
                .status(BookingStatus.WAITING)
                .build();
    }

    private CreateBookingDto getForcreateBookingDto() {
        return CreateBookingDto.builder()
                .start(LocalDateTime.now().plusMinutes(5))
                .end(LocalDateTime.now().plusMinutes(10))
                .itemId(1L)
                .build();
    }

    private MockHttpServletResponse createBookingResponse(Long bookerId,
                                                          CreateBookingDto dto) throws Exception {
        result = mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .header("X-Sharer-User-Id", bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Добавить бронирование")
    @Test
    void shouldCreateBooking() throws Exception {
        creationDto = getForcreateBookingDto();
        bookingDto = createBookingDto();
        when(bookingClient.createBooking(anyLong(), any(CreateBookingDto.class)))
                .thenReturn(ResponseEntity.ok(bookingDto));

        response = createBookingResponse(1L, creationDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(bookingDto), response.getContentAsString());

        verify(bookingClient, times(1))
                .createBooking(anyLong(), any(CreateBookingDto.class));
        verifyNoMoreInteractions(bookingClient);
    }

    @DisplayName("Статус 400 при добавлении бронирования")
    @Test
    public void shouldReturn400WhenWrongCreateBooking() throws Exception {
        // Проверка валидации bookerId
        creationDto = getForcreateBookingDto();
        assertEquals(400, createBookingResponse(0L, creationDto).getStatus());
        assertEquals(400, createBookingResponse(-1L, creationDto).getStatus());

        //Проверка валидации времени начала
        CreateBookingDto dtoNullStart = creationDto.withStart(null);
        assertEquals(400, createBookingResponse(1L, dtoNullStart).getStatus());

        CreateBookingDto dtoNowStart = creationDto.withStart(LocalDateTime.now());
        assertEquals(400, createBookingResponse(1L, dtoNowStart).getStatus());

        CreateBookingDto dtoPastStart = creationDto.withStart(LocalDateTime.now().minusMinutes(5));
        assertEquals(400, createBookingResponse(1L, dtoPastStart).getStatus());

        //Проверка валидации времени конца
        CreateBookingDto dtoNullEnd = creationDto.withEnd(null);
        assertEquals(400, createBookingResponse(1L, dtoNullEnd).getStatus());

        CreateBookingDto dtoNowEnd = creationDto.withEnd(LocalDateTime.now());
        assertEquals(400, createBookingResponse(1L, dtoNowEnd).getStatus());

        CreateBookingDto dtoPastEnd = creationDto.withEnd(LocalDateTime.now().minusMinutes(5));
        assertEquals(400, createBookingResponse(1L, dtoPastEnd).getStatus());

        // Проверка времени: начало должно быть раньше конца
        final CreateBookingDto creationWrongTimeDto = creationDto.withEnd(LocalDateTime.now().plusMinutes(2));
        assertEquals(400, createBookingResponse(1L, creationWrongTimeDto).getStatus());

        //Проверка валидации itemId
        CreateBookingDto dtoNullItemId = creationDto.withItemId(null);
        assertEquals(400, createBookingResponse(1L, dtoNullItemId).getStatus());

        CreateBookingDto dto0ItemId = creationDto.withItemId(0L);
        assertEquals(400, createBookingResponse(1L, dto0ItemId).getStatus());

        CreateBookingDto dtoNegativeItemId = creationDto.withItemId(-1L);
        assertEquals(400, createBookingResponse(1L, dtoNegativeItemId).getStatus());

        verifyNoInteractions(bookingClient);
    }

    private MockHttpServletResponse updateStatusResponse(Long ownerId,
                                                         Long bookingId,
                                                         Boolean approved) throws Exception {
        result = mvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", String.valueOf(approved))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Обновить статуса бронирования")
    @Test
    void shouldUpdateStatus() throws Exception {
        bookingDto = createBookingDto();
        when(bookingClient.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(bookingDto));

        response = updateStatusResponse(1L, 1L, true);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(bookingDto), response.getContentAsString());

        verify(bookingClient, times(1))
                .updateStatus(anyLong(), anyLong(), anyBoolean());
        verifyNoMoreInteractions(bookingClient);
    }

    @DisplayName("Статус 400 при обновлении бронирования")
    @Test
    public void shouldReturn400WhenWrongUpdateStatus() throws Exception {
        // Проверка валидации ownerId
        assertEquals(400, updateStatusResponse(0L, 1L, true).getStatus());
        assertEquals(400, updateStatusResponse(-1L, 1L, true).getStatus());

        // Проверка валидации bookingId
        assertEquals(400, updateStatusResponse(1L, 0L, true).getStatus());
        assertEquals(400, updateStatusResponse(1L, -1L, true).getStatus());

        verifyNoInteractions(bookingClient);
    }

    private MockHttpServletResponse deleteBookingResponse(Long bookerId, Long bookingId) throws Exception {
        result = mvc.perform(delete("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", bookerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Удалить бронирование")
    @Test
    void shouldDelete() throws Exception {
        when(bookingClient.delete(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(1L));

        response = deleteBookingResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(1L), response.getContentAsString());

        verify(bookingClient, times(1))
                .delete(anyLong(), anyLong());
        verifyNoMoreInteractions(bookingClient);
    }

    @DisplayName("Статус 400 при удалении бронирования")
    @Test
    public void shouldReturn400WhenWrongDeleteBooking() throws Exception {
        // Проверка валидации bookerId
        assertEquals(400, deleteBookingResponse(0L, 1L).getStatus());
        assertEquals(400, deleteBookingResponse(-1L, 1L).getStatus());

        // Проверка валидации bookingId
        assertEquals(400, deleteBookingResponse(1L, 0L).getStatus());
        assertEquals(400, deleteBookingResponse(1L, -1L).getStatus());

        verifyNoInteractions(bookingClient);
    }

    private MockHttpServletResponse findByIdBookingResponse(Long userId, Long bookingId) throws Exception {
        result = mvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить бронирование по id")
    @Test
    void shouldFindById() throws Exception {
        bookingDto = createBookingDto();
        when(bookingClient.findById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok(bookingDto));

        response = findByIdBookingResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(bookingDto), response.getContentAsString());

        verify(bookingClient, times(1))
                .findById(anyLong(), anyLong());
        verifyNoMoreInteractions(bookingClient);
    }

    @DisplayName("Статус 400 при получении бронирования по id")
    @Test
    public void shouldReturn400WhenWrongFindByIdBooking() throws Exception {
        // Проверка валидации userId
        assertEquals(400, findByIdBookingResponse(0L, 1L).getStatus());
        assertEquals(400, findByIdBookingResponse(-1L, 1L).getStatus());

        // Проверка валидации bookingId
        assertEquals(400, findByIdBookingResponse(1L, 0L).getStatus());
        assertEquals(400, findByIdBookingResponse(1L, -1L).getStatus());

        verifyNoInteractions(bookingClient);
    }

    private MockHttpServletResponse findAllByBookerResponse(Long bookerId,
                                                            String state,
                                                            int from,
                                                            int size) throws Exception {
        result = mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить бронирования по id бронирующего")
    @Test
    void shouldFindAllByBooker() throws Exception {
        bookingDto = createBookingDto();
        when(bookingClient.findAllByBooker(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of(bookingDto)));

        response = findAllByBookerResponse(1L, "WAITING", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        response = findAllByBookerResponse(1L, "", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        response = findAllByBookerResponse(1L, null, 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        verify(bookingClient, times(3))
                .findAllByBooker(anyLong(), any(BookingState.class), anyInt(), anyInt());
        verifyNoMoreInteractions(bookingClient);
    }

    @DisplayName("Статус 400 при получении бронирований по id бронирующего")
    @Test
    public void shouldReturn400WhenWrongFindAllByBooker() throws Exception {
        // Проверка валидации bookerId
        assertEquals(400, findAllByBookerResponse(0L, "WAITING", 0, 5).getStatus());
        assertEquals(400, findAllByBookerResponse(-1L, "WAITING", 0, 5).getStatus());

        // Проверка валидации from
        assertEquals(400, findAllByBookerResponse(1L, "WAITING", -1, 5).getStatus());

        // Проверка валидации size
        assertEquals(400, findAllByBookerResponse(1L, "WAITING", 0, 0).getStatus());
        assertEquals(400, findAllByBookerResponse(1L, "WAITING", 0, -1).getStatus());

        verifyNoInteractions(bookingClient);
    }

    private MockHttpServletResponse findAllByOwnerBookingResponse(Long ownerId,
                                                                  String state,
                                                                  int from,
                                                                  int size) throws Exception {
        result = mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return result.getResponse();
    }

    @DisplayName("Получить бронирования по id владельца предметов")
    @Test
    void shouldFindAllByOwnerBooking() throws Exception {
        bookingDto = createBookingDto();
        when(bookingClient.findAllByOwner(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of(bookingDto)));

        response = findAllByOwnerBookingResponse(1L, "WAITING", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        response = findAllByOwnerBookingResponse(1L, "", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        response = findAllByOwnerBookingResponse(1L, null, 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        verify(bookingClient, times(3))
                .findAllByOwner(anyLong(), any(BookingState.class), anyInt(), anyInt());
        verifyNoMoreInteractions(bookingClient);
    }

    @DisplayName("Статус 400 при получении бронирований по id владельца предметов")
    @Test
    public void shouldReturn400WhenWrongFindAllByOwnerBooking() throws Exception {
        // Проверка валидации ownerId
        assertEquals(400, findAllByOwnerBookingResponse(0L, "WAITING", 0, 5).getStatus());
        assertEquals(400, findAllByOwnerBookingResponse(-1L, "WAITING", 0, 5).getStatus());

        // Проверка валидации from
        assertEquals(400, findAllByOwnerBookingResponse(1L, "WAITING", -1, 5).getStatus());

        // Проверка валидации size
        assertEquals(400, findAllByOwnerBookingResponse(1L, "WAITING", 0, 0).getStatus());
        assertEquals(400, findAllByOwnerBookingResponse(1L, "WAITING", 0, -1).getStatus());

        verifyNoInteractions(bookingClient);
    }
}
