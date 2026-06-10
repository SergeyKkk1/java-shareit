package ru.practicum.shareit.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void startValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void validUserHasNoViolations() {
        assertThat(validator.validate(new UserDto(null, "Alice", "alice@mail.com"))).isEmpty();
    }

    @Test
    void userWithBlankNameIsInvalid() {
        assertThat(validator.validate(new UserDto(null, " ", "alice@mail.com"))).isNotEmpty();
    }

    @Test
    void userWithBlankEmailIsInvalid() {
        assertThat(validator.validate(new UserDto(null, "Alice", ""))).isNotEmpty();
    }

    @Test
    void userWithMalformedEmailIsInvalid() {
        assertThat(validator.validate(new UserDto(null, "Alice", "not-an-email"))).isNotEmpty();
    }

    @Test
    void validItemHasNoViolations() {
        assertThat(validator.validate(new ItemDto(null, "Drill", "Powerful", true, null))).isEmpty();
    }

    @Test
    void itemWithBlankNameIsInvalid() {
        assertThat(validator.validate(new ItemDto(null, "", "Powerful", true, null))).isNotEmpty();
    }

    @Test
    void itemWithBlankDescriptionIsInvalid() {
        assertThat(validator.validate(new ItemDto(null, "Drill", "", true, null))).isNotEmpty();
    }

    @Test
    void itemWithNullAvailableIsInvalid() {
        assertThat(validator.validate(new ItemDto(null, "Drill", "Powerful", null, null))).isNotEmpty();
    }

    @Test
    void validBookingHasNoViolations() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void bookingWithNullStartIsInvalid() {
        BookItemRequestDto dto = new BookItemRequestDto(1L, null, LocalDateTime.now().plusDays(2));
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void bookingWithNullEndIsInvalid() {
        BookItemRequestDto dto = new BookItemRequestDto(1L, LocalDateTime.now().plusDays(1), null);
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void bookingWithPastStartIsInvalid() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void bookingWithPastEndIsInvalid() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().minusDays(1));
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void bookingWithStartEqualToEndIsInvalid() {
        LocalDateTime moment = LocalDateTime.now().plusDays(1);
        assertThat(validator.validate(new BookItemRequestDto(1L, moment, moment))).isNotEmpty();
    }

    @Test
    void bookingWithEndBeforeStartIsInvalid() {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));
        assertThat(validator.validate(dto)).isNotEmpty();
    }

    @Test
    void validRequestHasNoViolations() {
        assertThat(validator.validate(new ItemRequestCreateDto("Need a drill"))).isEmpty();
    }

    @Test
    void requestWithBlankDescriptionIsInvalid() {
        assertThat(validator.validate(new ItemRequestCreateDto(" "))).isNotEmpty();
    }

    @Test
    void validCommentHasNoViolations() {
        assertThat(validator.validate(new CommentDto("Great tool"))).isEmpty();
    }

    @Test
    void commentWithBlankTextIsInvalid() {
        assertThat(validator.validate(new CommentDto(""))).isNotEmpty();
    }
}
