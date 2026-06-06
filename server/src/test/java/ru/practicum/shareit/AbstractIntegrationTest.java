package ru.practicum.shareit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected BookingRepository bookingRepository;

    @Autowired
    protected ItemRequestRepository itemRequestRepository;

    @Autowired
    protected CommentRepository commentRepository;

    protected User createUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    protected Item createItem(String name, String description, boolean available, User owner) {
        return itemRepository.save(new Item(null, name, description, available, owner, null));
    }

    protected Item createItem(String name, String description, boolean available, User owner, ItemRequest request) {
        return itemRepository.save(new Item(null, name, description, available, owner, request));
    }

    protected ItemRequest createRequest(String description, User requestor) {
        return itemRequestRepository.save(new ItemRequest(null, description, requestor, LocalDateTime.now()));
    }

    protected Booking createBooking(Item item, User booker, LocalDateTime start, LocalDateTime end,
                                    BookingStatus status) {
        return bookingRepository.save(new Booking(null, start, end, item, booker, status));
    }

    protected Comment createComment(String text, Item item, User author) {
        return commentRepository.save(new Comment(null, text, item, author, LocalDateTime.now()));
    }
}
