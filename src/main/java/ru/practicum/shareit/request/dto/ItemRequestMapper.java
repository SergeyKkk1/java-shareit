package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "requestorId", source = "requestor.id")
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "requestor", ignore = true)
    ItemRequest toEntity(ItemRequestDto dto);
}
