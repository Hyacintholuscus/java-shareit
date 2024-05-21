package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable
                                  @Positive(message = "User's id should be positive")
                                  Long id,
                              @RequestBody Map<String, Object> fields) {
        UserDto userDto = userService.getById(id);
        fields.remove("id");
        fields.forEach((k, v) -> {
            Field field = ReflectionUtils.findField(UserDto.class, k);
            field.setAccessible(true);
            ReflectionUtils.setField(field, userDto, v);
        });
        return userService.update(userDto);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable
                                   @Positive(message = "User's id should be positive")
                                   Long id) {
        return userService.getById(id);
    }

    @DeleteMapping("/{id}")
    public Long deleteUserById(@PathVariable
                                   @Positive(message = "User's id should be positive")
                                   Long id) {
        return userService.delete(id);
    }
}