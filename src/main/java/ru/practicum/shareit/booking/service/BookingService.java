package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto create(BookingCreateDto dto, Long userId);

    BookingDto approve(Long bookingId, boolean approved, Long userId);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getByBooker(Long userId, BookingState state);

    List<BookingDto> getByOwner(Long ownerId, BookingState state);
}
