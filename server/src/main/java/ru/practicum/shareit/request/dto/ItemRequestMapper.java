package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "items", ignore = true)
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "requestor", source = "requestor")
    @Mapping(target = "created", source = "created")
    ItemRequest toEntity(ItemRequestCreateDto dto, User requestor, LocalDateTime created);
}
