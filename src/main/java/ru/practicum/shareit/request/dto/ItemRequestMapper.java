package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    ItemRequestDto toDto(ItemRequest itemRequest);

    ItemRequest toEntity(ItemRequestDto dto);
}
