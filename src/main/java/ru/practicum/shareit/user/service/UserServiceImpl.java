package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper mapper;

    @Override
    public UserDto create(UserDto userDto) {
        User user = userStorage.create(mapper.toUser(userDto));
        return mapper.toDto(user);
    }

    @Override
    public UserDto update(UserDto userDto) {
        User updatedUser = userStorage.update(mapper.toUser(userDto));
        return mapper.toDto(updatedUser);
    }

    @Override
    public Long delete(Long id) {
        return userStorage.delete(id);
    }

    @Override
    public UserDto getById(Long id) {
        return mapper.toDto(userStorage.getById(id));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
