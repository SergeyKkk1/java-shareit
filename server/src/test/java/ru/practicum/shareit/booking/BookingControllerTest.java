package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private BookingService bookingService;

    private BookingDto bookingDto() {
        ItemDto item = new ItemDto(1L, "Bike", "Nice", true, null, null, null, null);
        UserDto booker = new UserDto(2L, "Booker", "booker@mail.com");
        return new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING, item, booker);
    }

    @Test
    void createReturnsBooking() throws Exception {
        when(bookingService.create(any(), eq(2L))).thenReturn(bookingDto());
        BookingCreateDto request = new BookingCreateDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(1))
                .andExpect(jsonPath("$.booker.id").value(2));
    }

    @Test
    void approveReturnsBooking() throws Exception {
        when(bookingService.approve(eq(1L), eq(true), eq(1L))).thenReturn(bookingDto());

        mvc.perform(patch("/bookings/1").param("approved", "true").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdReturnsBooking() throws Exception {
        when(bookingService.getById(1L, 2L)).thenReturn(bookingDto());

        mvc.perform(get("/bookings/1").header(USER_ID_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.name").value("Bike"));
    }

    @Test
    void getByBookerReturnsList() throws Exception {
        when(bookingService.getByBooker(eq(2L), any())).thenReturn(List.of(bookingDto()));

        mvc.perform(get("/bookings").param("state", "ALL").header(USER_ID_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByOwnerReturnsList() throws Exception {
        when(bookingService.getByOwner(eq(1L), any())).thenReturn(List.of(bookingDto()));

        mvc.perform(get("/bookings/owner").param("state", "ALL").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].booker.id").value(2));
    }
}
