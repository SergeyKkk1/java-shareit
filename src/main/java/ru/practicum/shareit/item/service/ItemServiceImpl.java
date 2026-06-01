package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto create(ItemDto dto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + ownerId));
        Item item = itemMapper.toEntity(dto);
        item.setId(null);
        item.setOwner(owner);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto dto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId));
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can edit item");
        }
        if (dto.getName() != null && !dto.getName().isBlank()) {
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setIsAvailable(dto.getAvailable());
        }
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId));
        ItemDto dto = itemMapper.toDto(item);
        dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(itemId).stream()
                .map(commentMapper::toDto)
                .toList());
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findFirstByItemIdAndStatusAndStartDateBeforeOrderByStartDateDesc(
                    itemId, BookingStatus.APPROVED, now)
                    .ifPresent(b -> dto.setLastBooking(b.getStartDate()));
            bookingRepository.findFirstByItemIdAndStatusAndStartDateAfterOrderByStartDateAsc(
                    itemId, BookingStatus.APPROVED, now)
                    .ifPresent(b -> dto.setNextBooking(b.getStartDate()));
        }
        return dto;
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<CommentDto>> commentsByItem = commentRepository.findByItemIdInOrderByCreatedDesc(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())));
        Map<Long, List<Booking>> bookingsByItem = bookingRepository
                .findByItemIdInAndStatusOrderByStartDateAsc(itemIds, BookingStatus.APPROVED).stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        return items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setComments(commentsByItem.getOrDefault(item.getId(), List.of()));
                    List<Booking> bookings = bookingsByItem.getOrDefault(item.getId(), List.of());
                    bookings.stream()
                            .filter(b -> b.getStartDate().isBefore(now))
                            .reduce((a, b) -> b)
                            .ifPresent(b -> dto.setLastBooking(b.getStartDate()));
                    bookings.stream()
                            .filter(b -> b.getStartDate().isAfter(now))
                            .findFirst()
                            .ifPresent(b -> dto.setNextBooking(b.getStartDate()));
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId));
        LocalDateTime now = LocalDateTime.now();
        boolean hasPastBooking = bookingRepository
                .existsByBorrowerIdAndItemIdAndStatusAndEndDateBefore(
                        userId, itemId, BookingStatus.APPROVED, now);
        if (!hasPastBooking) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User " + userId + " has no completed booking for item " + itemId);
        }
        Comment comment = commentMapper.toEntity(dto, item, author, now);
        return commentMapper.toDto(commentRepository.save(comment));
    }
}
