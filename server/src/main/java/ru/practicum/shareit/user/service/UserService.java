package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto save(UserDto userDto);

    Long delete(Long id);

    UserDto getById(Long id);

    List<UserDto> getAll();
}
