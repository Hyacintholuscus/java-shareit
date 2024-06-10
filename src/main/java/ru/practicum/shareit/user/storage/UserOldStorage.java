package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserOldStorage {
    User create(User user);

    User update(User user);

    Long delete(Long id);

    User getById(Long id);

    List<User> getAll();

    boolean contains(Long id);
}
