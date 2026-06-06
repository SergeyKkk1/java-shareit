package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> itemJson;

    @Autowired
    private JacksonTester<BookingDto> bookingJson;

    @Autowired
    private JacksonTester<BookingCreateDto> bookingCreateJson;

    @Autowired
    private JacksonTester<ItemRequestDto> requestJson;

    @Autowired
    private JacksonTester<CommentDto> commentJson;

    @Test
    void serializeItemDtoExposesAvailableAndRequestId() throws Exception {
        ItemDto dto = new ItemDto(1L, "Drill", "Powerful drill", true, 2L, null, null, List.of());

        JsonContent<ItemDto> json = itemJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo("Powerful drill");
        assertThat(json).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(json).extractingJsonPathNumberValue("$.requestId").isEqualTo(2);
        assertThat(json).hasJsonPath("$.comments");
    }

    @Test
    void serializeBookingDtoExposesNestedItemAndBooker() throws Exception {
        ItemDto item = new ItemDto(1L, "Bike", "Nice", true, null, null, null, null);
        UserDto booker = new UserDto(2L, "Booker", "booker@mail.com");
        BookingDto dto = new BookingDto(1L, LocalDateTime.of(2030, 1, 1, 10, 0),
                LocalDateTime.of(2030, 1, 2, 10, 0), BookingStatus.WAITING, item, booker);

        JsonContent<BookingDto> json = bookingJson.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(json).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.item.name").isEqualTo("Bike");
        assertThat(json).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(json).extractingJsonPathStringValue("$.start").startsWith("2030-01-01T10:00");
    }

    @Test
    void deserializeBookingCreateDtoParsesDates() throws Exception {
        String content = "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}";

        BookingCreateDto dto = bookingCreateJson.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 1, 2, 10, 0));
    }

    @Test
    void serializeItemRequestDtoExposesItemsWithOwnerId() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Need a drill", LocalDateTime.of(2030, 1, 1, 10, 0),
                List.of(new ItemAnswerDto(5L, "Drill", 9L)));

        JsonContent<ItemRequestDto> json = requestJson.write(dto);

        assertThat(json).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(json).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(json).hasJsonPath("$.created");
        assertThat(json).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(5);
        assertThat(json).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(json).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(9);
    }

    @Test
    void serializeCommentDtoExposesAuthorName() throws Exception {
        CommentDto dto = new CommentDto(1L, "Great tool", "Alice", LocalDateTime.of(2030, 1, 1, 10, 0));

        JsonContent<CommentDto> json = commentJson.write(dto);

        assertThat(json).extractingJsonPathStringValue("$.text").isEqualTo("Great tool");
        assertThat(json).extractingJsonPathStringValue("$.authorName").isEqualTo("Alice");
        assertThat(json).hasJsonPath("$.created");
    }
}
