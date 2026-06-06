package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    private BookItemRequestDto validRequest() {
        return new BookItemRequestDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
    }

    @Test
    void createValidBookingDelegatesToClient() throws Exception {
        when(bookingClient.create(eq(2L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createBookingWithNullStartIsRejected() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L, null, LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }

    @Test
    void createBookingWithPastEndIsRejected() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).create(anyLong(), any());
    }

    @Test
    void approveDelegatesToClient() throws Exception {
        when(bookingClient.approve(1L, 1L, true)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(patch("/bookings/1").param("approved", "true").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }

    @Test
    void getByIdDelegatesToClient() throws Exception {
        when(bookingClient.getById(2L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(get("/bookings/1").header(USER_ID_HEADER, 2))
                .andExpect(status().isOk());
    }

    @Test
    void getByBookerDelegatesToClient() throws Exception {
        when(bookingClient.getByBooker(2L, "ALL")).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mvc.perform(get("/bookings").header(USER_ID_HEADER, 2))
                .andExpect(status().isOk());
    }

    @Test
    void getByOwnerDelegatesToClient() throws Exception {
        when(bookingClient.getByOwner(1L, "ALL")).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mvc.perform(get("/bookings/owner").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());
    }
}
