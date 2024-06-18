package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    @Query("select it from Item it where it.available = true and (lower(it.name) like lower(concat('%', ?1,'%')) " +
            "or lower(it.description) like lower(concat('%', ?1,'%')))")
    List<Item> search(String text);

    @Query(value = "SELECT * FROM Items it WHERE it.owner_id = ?1 and it.id = ?2", nativeQuery = true)
    Optional<Item> findItemByOwnerId(Long ownerId, Long itemId);
}
