package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper requestMapper;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        log.info("Creating item request {} for user {}", dto, userId);
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        ItemRequestDto result = requestMapper.toDto(requestRepository.save(request));
        result.setItems(List.of());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getByRequestor(Long userId) {
        log.info("Getting item requests of user {}", userId);
        ensureUserExists(userId);
        return toDtosWithItems(requestRepository.findByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAll(Long userId) {
        log.info("Getting all item requests for user {}", userId);
        ensureUserExists(userId);
        return toDtosWithItems(requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Getting item request {} for user {}", requestId, userId);
        ensureUserExists(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found: " + requestId));
        ItemRequestDto dto = requestMapper.toDto(request);
        dto.setItems(itemRepository.findByRequestId(requestId).stream()
                .map(this::toAnswer)
                .toList());
        return dto;
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<ItemAnswerDto>> answersByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(this::toAnswer, Collectors.toList())));
        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = requestMapper.toDto(request);
                    dto.setItems(answersByRequest.getOrDefault(request.getId(), List.of()));
                    return dto;
                })
                .toList();
    }

    private ItemAnswerDto toAnswer(Item item) {
        return new ItemAnswerDto(item.getId(), item.getName(), item.getOwner().getId());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId);
        }
    }
}
