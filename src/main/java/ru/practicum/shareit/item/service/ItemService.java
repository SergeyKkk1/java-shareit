package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(ItemDto dto, Long ownerId);

    ItemDto update(Long itemId, ItemDto dto, Long ownerId);

    ItemDto getById(Long itemId);

    List<ItemDto> getByOwner(Long ownerId);

    List<ItemDto> search(String text);
}
