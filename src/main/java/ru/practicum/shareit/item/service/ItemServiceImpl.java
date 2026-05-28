package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(ItemDto dto, Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + ownerId);
        }
        Item item = itemMapper.toEntity(dto);
        item.setId(null);
        item.setOwnerId(ownerId);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto dto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId));
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can edit item");
        }
        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + itemId));
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        return itemRepository.findAll().stream()
                .filter(i -> ownerId.equals(i.getOwnerId()))
                .map(itemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String needle = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> (i.getName() != null && i.getName().toLowerCase().contains(needle))
                        || (i.getDescription() != null && i.getDescription().toLowerCase().contains(needle)))
                .map(itemMapper::toDto)
                .toList();
    }
}
