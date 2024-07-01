package ru.practicum.shareitgate.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareitgate.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Map;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос создания пользователя");
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable
                                             @Positive(message = "User's id should be positive")
                                             Long id,
                                             @RequestBody Map<String, Object> fields) {
        log.info("Запрос обновления пользователя с id {}", id);
        return userClient.updateUser(id, fields);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Запрос получения всех пользователей");
        return userClient.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable
                                             @Positive(message = "User's id should be positive")
                                             Long id) {
        log.info("Запрос получения пользователя с id {}", id);
        return userClient.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable
                               @Positive(message = "User's id should be positive")
                               Long id) {
        log.info("Запрос удаления пользователя с id {}", id);
        return userClient.deleteUserById(id);
    }
}
