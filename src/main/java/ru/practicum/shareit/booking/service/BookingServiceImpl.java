package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto create(BookingCreateDto dto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + dto.getItemId()));
        if (!Boolean.TRUE.equals(item.getIsAvailable())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not available for booking");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner cannot book their own item");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "end must be after start");
        }
        Booking booking = bookingMapper.toEntity(dto, item, booker, BookingStatus.WAITING);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(Long bookingId, boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only item owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is not awaiting approval");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));
        boolean isBooker = booking.getBorrower().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not visible to user " + userId);
        }
        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
        return bookingRepository.findByBookerAndState(userId, state.name(), LocalDateTime.now()).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, BookingState state) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + ownerId);
        }
        return bookingRepository.findByOwnerAndState(ownerId, state.name(), LocalDateTime.now()).stream()
                .map(bookingMapper::toDto)
                .toList();
    }
}
