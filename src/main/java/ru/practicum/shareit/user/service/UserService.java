package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserDto dto);

    UserDto getById(Long id);

    List<UserDto> getAll();

    UserDto update(Long id, UserDto dto);

    void delete(Long id);
}
