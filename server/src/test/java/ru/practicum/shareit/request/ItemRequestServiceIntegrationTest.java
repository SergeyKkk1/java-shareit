package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.AbstractIntegrationTest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemRequestServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Test
    void createPersistsRequestWithEmptyItems() {
        User requestor = createUser("Req", "req1@mail.com");

        ItemRequestDto created = itemRequestService.create(requestor.getId(), new ItemRequestCreateDto("Need a drill"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void createForMissingUserThrowsNotFound() {
        assertThatThrownBy(() -> itemRequestService.create(9999L, new ItemRequestCreateDto("X")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getByRequestorReturnsOwnRequestsNewestFirstWithAnswers() {
        User requestor = createUser("Req", "req2@mail.com");
        User owner = createUser("Owner", "own2@mail.com");
        LocalDateTime now = LocalDateTime.now();
        ItemRequest older = requestRepository.save(new ItemRequest(null, "older", requestor, now.minusHours(2)));
        ItemRequest newer = requestRepository.save(new ItemRequest(null, "newer", requestor, now.minusHours(1)));
        createItem("Drill", "Drill answer", true, owner, newer);

        List<ItemRequestDto> result = itemRequestService.getByRequestor(requestor.getId());

        assertThat(result).extracting(ItemRequestDto::getDescription).containsExactly("newer", "older");
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(result.get(0).getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
        assertThat(result.get(1).getItems()).isEmpty();
    }

    @Test
    void getByRequestorForMissingUserThrowsNotFound() {
        assertThatThrownBy(() -> itemRequestService.getByRequestor(9999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getAllReturnsOnlyOtherUsersRequestsNewestFirst() {
        User requestor = createUser("Req", "req3@mail.com");
        User otherUser = createUser("Other", "other3@mail.com");
        LocalDateTime now = LocalDateTime.now();
        requestRepository.save(new ItemRequest(null, "mine", requestor, now.minusHours(2)));
        requestRepository.save(new ItemRequest(null, "theirs-old", otherUser, now.minusHours(3)));
        requestRepository.save(new ItemRequest(null, "theirs-new", otherUser, now.minusHours(1)));

        List<ItemRequestDto> result = itemRequestService.getAll(requestor.getId());

        assertThat(result).extracting(ItemRequestDto::getDescription).containsExactly("theirs-new", "theirs-old");
    }

    @Test
    void getByIdReturnsRequestWithAnswers() {
        User requestor = createUser("Req", "req4@mail.com");
        User owner = createUser("Owner", "own4@mail.com");
        ItemRequest request = createRequest("Need a saw", requestor);
        createItem("Saw", "Saw answer", true, owner, request);

        ItemRequestDto dto = itemRequestService.getById(owner.getId(), request.getId());

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Saw");
    }

    @Test
    void getByIdForMissingRequestThrowsNotFound() {
        User requestor = createUser("Req", "req5@mail.com");

        assertThatThrownBy(() -> itemRequestService.getById(requestor.getId(), 9999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getByRequestorWithNoRequestsReturnsEmptyList() {
        User requestor = createUser("Req", "req6@mail.com");

        List<ItemRequestDto> result = itemRequestService.getByRequestor(requestor.getId());

        assertThat(result).isEmpty();
    }
}
