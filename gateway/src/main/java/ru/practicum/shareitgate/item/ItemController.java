package ru.practicum.shareitgate.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareitgate.item.dto.CreateCommentDto;
import ru.practicum.shareitgate.item.dto.CreateItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id")
                                                 @Positive(message = "User's id should be positive")
                                                 Long userId,
                                             @Valid @RequestBody CreateItemDto creationDto) {
        log.info("Запрос создания предмета от пользователя с id {}", userId);
        return itemClient.createItem(userId, creationDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable
                                    @Positive(message = "Item's id should be positive")
                                    Long itemId,
                                    @RequestHeader("X-Sharer-User-Id")
                                    @Positive(message = "User's id should be positive")
                                    Long authorId,
                                    @RequestBody @Valid CreateCommentDto creationDto) {
        log.info("Запрос создания комментария к предмету с id {} от пользователя с id {}",
                itemId, authorId);
        return itemClient.createComment(itemId, authorId, creationDto);
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable
                              @Positive(message = "Item's id should be positive")
                              Long itemId,
                              @RequestHeader("X-Sharer-User-Id")
                              @Positive(message = "User's id should be positive")
                              Long userId,
                              @RequestBody Map<String, Object> fields) {
        log.info("Запрос обновления предмета с id {} от пользователя с id {}", itemId, userId);
        return itemClient.updateItem(itemId, userId, fields);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable
                               @Positive(message = "Item's id should be positive")
                               Long itemId,
                               @RequestHeader("X-Sharer-User-Id")
                               @Positive(message = "User's id should be positive")
                               Long userId) {
        log.info("Запрос получения предмета с id {} от пользователя с id {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUser(@RequestHeader("X-Sharer-User-Id")
                                        @Positive(message = "User's id should be positive")
                                        Long userId,
                                        @RequestParam(defaultValue = "0")
                                        @PositiveOrZero(message = "Parameter 'from' should be positive or zero")
                                        int from,
                                        @RequestParam(defaultValue = "10")
                                        @Positive(message = "Parameter 'size' should be positive")
                                        int size) {
        log.info("Запрос получения всех предметов пользователя с id {}", userId);
        return itemClient.getItemsByUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader("X-Sharer-User-Id")
                                                  @Positive(message = "User's id should be positive")
                                                  Long userId,
                                              @RequestParam String text,
                                              @RequestParam(defaultValue = "0")
                                                  @PositiveOrZero(message = "Parameter 'from' should be positive or zero")
                                                  int from,
                                              @RequestParam(defaultValue = "10")
                                                  @Positive(message = "Parameter 'size' should be positive")
                                                  int size) {
        log.info("Запрос поиска предметов от пользователя с id {}", userId);
        if (text.isBlank()) {
            return ResponseEntity.ok(new ArrayList<>());
        } else {
            return itemClient.searchItems(userId, text, from, size);
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader("X-Sharer-User-Id")
                           @Positive(message = "User's id should be positive")
                           Long userId,
                           @PathVariable
                           @Positive(message = "Item's id should be positive")
                           Long itemId) {
        log.info("Запрос удаления предмета с id {} пользователя с id {}", itemId, userId);
        return itemClient.deleteItem(userId, itemId);
    }
}
