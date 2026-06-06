package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ItemService itemService;

    private ItemDto newItemDto(String name, String description, Boolean available, Long requestId) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        return dto;
    }

    @Test
    void createPersistsItemForOwner() {
        User owner = createUser("Owner", "owner@mail.com");

        ItemDto created = itemService.create(newItemDto("Drill", "Powerful drill", true, null), owner.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Drill");
        assertThat(created.getAvailable()).isTrue();
        assertThat(created.getRequestId()).isNull();
    }

    @Test
    void createWithRequestIdLinksItemToRequest() {
        User requestor = createUser("Requestor", "req@mail.com");
        User owner = createUser("Owner", "owner2@mail.com");
        ItemRequest request = createRequest("Need a drill", requestor);

        ItemDto created = itemService.create(newItemDto("Drill", "Drill", true, request.getId()), owner.getId());

        assertThat(created.getRequestId()).isEqualTo(request.getId());
        Item stored = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(stored.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void createForMissingUserThrowsNotFound() {
        assertThatThrownBy(() -> itemService.create(newItemDto("X", "Y", true, null), 9999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateChangesEditableFields() {
        User owner = createUser("Owner", "owner3@mail.com");
        Item item = createItem("Old", "Old desc", false, owner);

        ItemDto patch = newItemDto("New", "New desc", true, null);
        ItemDto updated = itemService.update(item.getId(), patch, owner.getId());

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("New desc");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void updateByNonOwnerThrowsForbidden() {
        User owner = createUser("Owner", "owner4@mail.com");
        User stranger = createUser("Stranger", "stranger@mail.com");
        Item item = createItem("Item", "Desc", true, owner);

        assertThatThrownBy(() -> itemService.update(item.getId(), newItemDto("Hack", null, null, null), stranger.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
    }

    @Test
    void getByIdReturnsItemWithCommentsAndBookingsForOwner() {
        User owner = createUser("Owner", "owner5@mail.com");
        User booker = createUser("Booker", "booker5@mail.com");
        Item item = createItem("Saw", "Sharp saw", true, owner);
        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        createBooking(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);
        createComment("Great saw", item, booker);

        ItemDto dto = itemService.getById(item.getId(), owner.getId());

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
    }

    @Test
    void getByOwnerReturnsOwnedItemsWithComments() {
        User owner = createUser("Owner", "owner6@mail.com");
        User booker = createUser("Booker", "booker6@mail.com");
        Item item = createItem("Hammer", "Heavy hammer", true, owner);
        createComment("Solid", item, booker);

        List<ItemDto> items = itemService.getByOwner(owner.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getComments()).hasSize(1);
    }

    @Test
    void searchFindsAvailableItemsByText() {
        User owner = createUser("Owner", "owner7@mail.com");
        createItem("Cordless Drill", "For wood", true, owner);
        createItem("Hidden", "unavailable drill", false, owner);

        List<ItemDto> found = itemService.search("drill");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Cordless Drill");
    }

    @Test
    void searchWithBlankTextReturnsEmpty() {
        assertThat(itemService.search("  ")).isEmpty();
    }

    @Test
    void addCommentSucceedsAfterCompletedBooking() {
        User owner = createUser("Owner", "owner8@mail.com");
        User booker = createUser("Booker", "booker8@mail.com");
        Item item = createItem("Ladder", "Tall ladder", true, owner);
        LocalDateTime now = LocalDateTime.now();
        createBooking(item, booker, now.minusDays(3), now.minusDays(1), BookingStatus.APPROVED);

        CommentDto dto = new CommentDto();
        dto.setText("Worked well");
        CommentDto saved = itemService.addComment(item.getId(), booker.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAuthorName()).isEqualTo("Booker");
        assertThat(saved.getText()).isEqualTo("Worked well");
    }

    @Test
    void addCommentWithoutCompletedBookingThrowsBadRequest() {
        User owner = createUser("Owner", "owner9@mail.com");
        User stranger = createUser("Stranger", "stranger9@mail.com");
        Item item = createItem("Tent", "Big tent", true, owner);

        CommentDto dto = new CommentDto();
        dto.setText("No booking");
        assertThatThrownBy(() -> itemService.addComment(item.getId(), stranger.getId(), dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void updateWithNullFieldsKeepsExistingValues() {
        User owner = createUser("Owner", "owner11@mail.com");
        Item item = createItem("Keep", "Keep desc", true, owner);

        ItemDto updated = itemService.update(item.getId(), newItemDto(null, null, null, null), owner.getId());

        assertThat(updated.getName()).isEqualTo("Keep");
        assertThat(updated.getDescription()).isEqualTo("Keep desc");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void getByIdByNonOwnerDoesNotExposeBookings() {
        User owner = createUser("Owner", "owner12@mail.com");
        User viewer = createUser("Viewer", "viewer12@mail.com");
        Item item = createItem("Drill", "Desc", true, owner);
        LocalDateTime now = LocalDateTime.now();
        createBooking(item, viewer, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);

        ItemDto dto = itemService.getById(item.getId(), viewer.getId());

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }
}
