package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper mapper;

    @Override
    public UserDto create(UserDto userDto) {
        try {
            User user = userStorage.save(mapper.toUser(userDto));
            return mapper.toDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("This email is already in use.");
        }
    }

    @Override
    public UserDto update(UserDto userDto) {
        try {
            User updatedUser = userStorage.save(mapper.toUser(userDto));
            return mapper.toDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("This email is already in use.");
        }
    }

    @Override
    public Long delete(Long id) {
        userStorage.deleteById(id);
        return id;
    }

    @Override
    public UserDto getById(Long id) {
        User user = userStorage.findById(id).orElseThrow(() -> {
                log.error("Запрос получить несуществующего пользователя с id {}.", id);
                 return new NotFoundException(
                String.format("User with id %d is not exist.", id)
                );
        });
        return mapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
