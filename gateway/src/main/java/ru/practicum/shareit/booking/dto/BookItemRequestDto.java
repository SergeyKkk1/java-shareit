package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@StartBeforeEnd
public class BookItemRequestDto {

    @NotNull(message = "itemId is required")
    private Long itemId;

    @NotNull(message = "start is required")
    @FutureOrPresent(message = "start must not be in the past")
    private LocalDateTime start;

    @NotNull(message = "end is required")
    @Future(message = "end must be in the future")
    private LocalDateTime end;
}
