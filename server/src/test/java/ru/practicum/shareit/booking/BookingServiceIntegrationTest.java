package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookingService bookingService;

    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    @Test
    void createReturnsWaitingBooking() {
        User owner = createUser("Owner", "o1@mail.com");
        User booker = createUser("Booker", "b1@mail.com");
        Item item = createItem("Bike", "Mountain bike", true, owner);

        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void createForUnavailableItemThrowsBadRequest() {
        User owner = createUser("Owner", "o2@mail.com");
        User booker = createUser("Booker", "b2@mail.com");
        Item item = createItem("Bike", "Broken", false, owner);

        assertThatThrownBy(() -> bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void createByOwnerOfItemThrowsNotFound() {
        User owner = createUser("Owner", "o3@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);

        assertThatThrownBy(() -> bookingService.create(new BookingCreateDto(item.getId(), start, end), owner.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    void createWithEndNotAfterStartThrowsBadRequest() {
        User owner = createUser("Owner", "o4@mail.com");
        User booker = createUser("Booker", "b4@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);

        assertThatThrownBy(() -> bookingService.create(new BookingCreateDto(item.getId(), start, start), booker.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void approveByOwnerSetsApproved() {
        User owner = createUser("Owner", "o5@mail.com");
        User booker = createUser("Booker", "b5@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        BookingDto approved = bookingService.approve(created.getId(), true, owner.getId());

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveByNonOwnerThrowsForbidden() {
        User owner = createUser("Owner", "o6@mail.com");
        User booker = createUser("Booker", "b6@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        assertThatThrownBy(() -> bookingService.approve(created.getId(), true, booker.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getByIdVisibleToBookerAndOwner() {
        User owner = createUser("Owner", "o7@mail.com");
        User booker = createUser("Booker", "b7@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        assertThat(bookingService.getById(created.getId(), booker.getId()).getId()).isEqualTo(created.getId());
        assertThat(bookingService.getById(created.getId(), owner.getId()).getId()).isEqualTo(created.getId());
    }

    @Test
    void getByIdForStrangerThrowsNotFound() {
        User owner = createUser("Owner", "o8@mail.com");
        User booker = createUser("Booker", "b8@mail.com");
        User stranger = createUser("Stranger", "s8@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        assertThatThrownBy(() -> bookingService.getById(created.getId(), stranger.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getByBookerReturnsBookings() {
        User owner = createUser("Owner", "o9@mail.com");
        User booker = createUser("Booker", "b9@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        List<BookingDto> all = bookingService.getByBooker(booker.getId(), BookingState.ALL);
        List<BookingDto> future = bookingService.getByBooker(booker.getId(), BookingState.FUTURE);

        assertThat(all).hasSize(1);
        assertThat(future).hasSize(1);
    }

    @Test
    void getByBookerForMissingUserThrowsNotFound() {
        assertThatThrownBy(() -> bookingService.getByBooker(9999L, BookingState.ALL))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getByOwnerReturnsBookings() {
        User owner = createUser("Owner", "o10@mail.com");
        User booker = createUser("Booker", "b10@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        List<BookingDto> all = bookingService.getByOwner(owner.getId(), BookingState.ALL);
        List<BookingDto> waiting = bookingService.getByOwner(owner.getId(), BookingState.WAITING);

        assertThat(all).hasSize(1);
        assertThat(waiting).hasSize(1);
    }

    @Test
    void getByOwnerForMissingUserThrowsNotFound() {
        assertThatThrownBy(() -> bookingService.getByOwner(9999L, BookingState.ALL))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void approveAlreadyProcessedBookingThrowsBadRequest() {
        User owner = createUser("Owner", "o11@mail.com");
        User booker = createUser("Booker", "b11@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());
        bookingService.approve(created.getId(), true, owner.getId());

        assertThatThrownBy(() -> bookingService.approve(created.getId(), false, owner.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void approveRejectsWhenNotApproved() {
        User owner = createUser("Owner", "o12@mail.com");
        User booker = createUser("Booker", "b12@mail.com");
        Item item = createItem("Bike", "Nice", true, owner);
        BookingDto created = bookingService.create(new BookingCreateDto(item.getId(), start, end), booker.getId());

        BookingDto rejected = bookingService.approve(created.getId(), false, owner.getId());

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }
}
