package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userService.save(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id,
                              @RequestBody Map<String, Object> fields) {
        UserDto userDto = userService.getById(id);
        fields.forEach((k, v) -> {
            Field field = ReflectionUtils.findField(UserDto.class, k);
            field.setAccessible(true);
            ReflectionUtils.setField(field, userDto, v);
        });
        return userService.save(userDto);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @DeleteMapping("/{id}")
    public Long deleteUserById(@PathVariable Long id) {
        return userService.delete(id);
    }
}