package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createPersistsUserAndReturnsDtoWithId() {
        UserDto dto = new UserDto(null, "Alice", "alice@mail.com");

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Alice");
        assertThat(created.getEmail()).isEqualTo("alice@mail.com");
        assertThat(userRepository.findById(created.getId())).isPresent();
    }

    @Test
    void createWithDuplicateEmailThrowsConflict() {
        createUser("Bob", "dup@mail.com");

        assertThatThrownBy(() -> userService.create(new UserDto(null, "Other", "dup@mail.com")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void getByIdReturnsStoredUser() {
        User stored = createUser("Carol", "carol@mail.com");

        UserDto found = userService.getById(stored.getId());

        assertThat(found.getId()).isEqualTo(stored.getId());
        assertThat(found.getEmail()).isEqualTo("carol@mail.com");
    }

    @Test
    void getByIdMissingThrowsNotFound() {
        assertThrows(ResponseStatusException.class, () -> userService.getById(9999L));
    }

    @Test
    void getAllReturnsEveryUser() {
        createUser("Dan", "dan@mail.com");
        createUser("Eve", "eve@mail.com");

        List<UserDto> all = userService.getAll();

        assertThat(all).extracting(UserDto::getEmail)
                .contains("dan@mail.com", "eve@mail.com");
    }

    @Test
    void updateChangesNameAndEmail() {
        User stored = createUser("Frank", "frank@mail.com");

        UserDto updated = userService.update(stored.getId(), new UserDto(null, "Franklin", "franklin@mail.com"));

        assertThat(updated.getName()).isEqualTo("Franklin");
        assertThat(updated.getEmail()).isEqualTo("franklin@mail.com");
    }

    @Test
    void updateWithEmailOfAnotherUserThrowsConflict() {
        createUser("Grace", "grace@mail.com");
        User other = createUser("Heidi", "heidi@mail.com");

        assertThatThrownBy(() -> userService.update(other.getId(), new UserDto(null, null, "grace@mail.com")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
    }

    @Test
    void deleteRemovesUser() {
        User stored = createUser("Ivan", "ivan@mail.com");

        userService.delete(stored.getId());

        assertThat(userRepository.findById(stored.getId())).isEmpty();
    }

    @Test
    void updateOnlyNameLeavesEmailUnchanged() {
        User stored = createUser("Judy", "judy@mail.com");

        UserDto updated = userService.update(stored.getId(), new UserDto(null, "Judith", null));

        assertThat(updated.getName()).isEqualTo("Judith");
        assertThat(updated.getEmail()).isEqualTo("judy@mail.com");
    }

    @Test
    void updateWithSameEmailDoesNotConflict() {
        User stored = createUser("Kyle", "kyle@mail.com");

        UserDto updated = userService.update(stored.getId(), new UserDto(null, null, "kyle@mail.com"));

        assertThat(updated.getEmail()).isEqualTo("kyle@mail.com");
    }
}
