package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@RequiredArgsConstructor
@Repository
@Slf4j
public class InMemoryUserStorage implements UserOldStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private Long id = 0L;

    private Long createId() {
        return ++id;
    }

    @Override
    public User create(User user) {
        log.debug("Запрос создать нового пользователя.");

        // Проверка эл. почты
        String email = user.getEmail();
        if (emails.contains(email)) {
            throw new DuplicateException("This email is already in use.");
        } else emails.add(email);

        // Добавление нового пользователя
        User newUser = user.withId(createId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User update(User user) {
        log.debug("Запрос обновить пользователя с id {}.", user.getId());

        // Проверка эл. почты
        String pastEmail = users.get(user.getId()).getEmail();
        String newEmail = user.getEmail();
        if (!(newEmail.equals(pastEmail)) && (emails.contains(newEmail))) {
            throw new DuplicateException("This email is already in use.");
        } else if (!newEmail.equals(pastEmail)) {
            emails.remove(pastEmail);
            emails.add(newEmail);
        }

        // Обновление пользователя
        User updatedUser = users.get(user.getId())
                .withEmail(newEmail)
                .withName(user.getName());
        users.put(user.getId(), updatedUser);
        return user;
    }

    @Override
    public Long delete(Long id) {
        log.debug("Запрос удалить пользователя с id {}.", id);

        User user = users.remove(id);
        if (user != null) {
            emails.remove(user.getEmail());
        }
        return id;
    }

    @Override
    public User getById(Long id) {
        log.debug("Запрос получить пользователя с id {}.", id);

        User user = users.get(id);
        if (user == null) {
            log.error("Запрос получить несуществующего пользователя с id {}.", id);
            throw new NotFoundException(
                    String.format("User with id %d is not exist.", id)
            );
        } else return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean contains(Long id) {
        return users.containsKey(id);
    }
}
