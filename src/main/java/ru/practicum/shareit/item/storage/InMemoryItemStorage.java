package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemInMemoryStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Long>> usersItems = new HashMap<>();
    private Long id = 0L;

    private Long createId() {
        return ++id;
    }

    @Override
    public Item create(Long userId, Item item) {
        log.debug("Запрос создать новый предмет.");

        Item newItem = item.withId(createId())
                        //.withOwnerId(userId)
                        .withTenantIds(new ArrayList<>());
        items.put(newItem.getId(), newItem);

        usersItems.compute(userId, (id, itemsIds) -> {
           if (itemsIds == null) {
               itemsIds = new ArrayList<>();
           }
           itemsIds.add(newItem.getId());
           return itemsIds;
        });

        return newItem;
    }

    @Override
    public Item update(Item item) {
        log.debug("Запрос обновить предмет с id {}.", item.getId());

        Item updatedItem = items.get(item.getId())
                .withName(item.getName())
                .withDescription(item.getDescription())
                .withAvailable(item.getAvailable())
                .withTenantIds(new ArrayList<>());
        items.put(item.getId(), updatedItem);
        return updatedItem;
    }

    @Override
    public Long delete(Long userId, Long itemId) {
        log.debug("Запрос удалить предмет с id {} от пользователя с id {}.", itemId, userId);

        Item item = items.remove(itemId);
        /*if (item != null) {
            List<Long> itemIds = usersItems.get(item.getOwnerId());
            itemIds.remove(itemId);
        }*/
        return itemId;
    }

    @Override
    public Item getById(Long id) {
        log.debug("Запрос получить предмет с id {}.", id);

        Item item = items.get(id);
        if (item == null) {
            log.error("Запрос получить несуществующий предмет с id {}.", id);
            throw new NotFoundException(
                    String.format("Item with id %d is not exist.", id)
            );
        }
        return item;
    }

    @Override
    public List<Item> getByUserId(Long userId) {
        log.debug("Запрос получить список предметов пользователя с id {}.", userId);

        List<Long> itemIds = usersItems.get(userId);
        if ((itemIds != null) && (!itemIds.isEmpty())) {
            List<Item> itemList = new ArrayList<>();
            for (Long id : itemIds) {
                itemList.add(items.get(id));
            }
            return itemList;
        } else return new ArrayList<>();
    }

    @Override
    public List<Item> search(String text) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> (StringUtils.containsIgnoreCase(item.getName(), text)) ||
                        (StringUtils.containsIgnoreCase(item.getDescription(), text)))
                .collect(Collectors.toList());
    }
}
