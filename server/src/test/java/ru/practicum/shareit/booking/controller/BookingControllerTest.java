package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @MockBean
    private BookingService bookingService;

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
        when(bookingService.create(anyLong(), any(CreateBookingDto.class)))
                .thenReturn(bookingDto);

        response = createBookingResponse(1L, creationDto);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(bookingDto), response.getContentAsString());

        verify(bookingService, times(1))
                .create(anyLong(), any(CreateBookingDto.class));
        verifyNoMoreInteractions(bookingService);
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
        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        response = updateStatusResponse(1L, 1L, true);

        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(bookingDto), response.getContentAsString());

        verify(bookingService, times(1))
                .updateStatus(anyLong(), anyLong(), anyBoolean());
        verifyNoMoreInteractions(bookingService);
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
        when(bookingService.delete(anyLong(), anyLong()))
                .thenReturn(1L);

        response = deleteBookingResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(1L), response.getContentAsString());

        verify(bookingService, times(1))
                .delete(anyLong(), anyLong());
        verifyNoMoreInteractions(bookingService);
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
        when(bookingService.findById(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        response = findByIdBookingResponse(1L, 1L);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(bookingDto), response.getContentAsString());

        verify(bookingService, times(1))
                .findById(anyLong(), anyLong());
        verifyNoMoreInteractions(bookingService);
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
        when(bookingService.findAllByBooker(anyLong(), anyString(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(bookingDto));

        response = findAllByBookerResponse(1L, "All", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        verify(bookingService, times(1))
                .findAllByBooker(anyLong(), anyString(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(bookingService);
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
        when(bookingService.findAllByOwnerItems(anyLong(), anyString(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(bookingDto));

        response = findAllByOwnerBookingResponse(1L, "ALL", 0, 5);
        assertEquals(200, response.getStatus());
        assertEquals(mapper.writeValueAsString(List.of(bookingDto)), response.getContentAsString());

        verify(bookingService, times(1))
                .findAllByOwnerItems(anyLong(), anyString(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(bookingService);
    }
}