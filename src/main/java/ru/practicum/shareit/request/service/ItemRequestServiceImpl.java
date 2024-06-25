package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestMapper mapper;
    private final ItemRequestStorage itemRequestStorage;
    private final UserStorage userStorage;

    @Transactional(readOnly = true)
    private void checkUser(Long userId) {
        if (!userStorage.existsById(userId)) {
            log.error("NotFound. Запрос на действие с предметом от несуществующего пользователя с id {}.", userId);
            throw new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        }
    }

    @Override
    public ItemRequestDto create(Long userId,
                                 LocalDateTime creationDate,
                                 CreateItemRequestDto creationDto) {
        log.info("Создание запроса от пользователя с id {}", userId);

        checkUser(userId);
        ItemRequest request = mapper.toItemRequest(userId, creationDate, creationDto);
        return mapper.toDto(itemRequestStorage.save(request));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Получение запроса с id {} от пользователя с id {}", requestId, userId);

        checkUser(userId);
        ItemRequest request = itemRequestStorage.findById(requestId).orElseThrow(() -> {
            log.error("NotFound. Запрос получить несуществующий запрос с id {}.", requestId);
            return new NotFoundException(
                    String.format("ItemRequest with id %d is not exist.", requestId)
            );
        });
        return mapper.toDto(request);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getByOwnerId(Long userId) {
        log.info("Получение запросов от пользователя с id {}", userId);

        User owner = userStorage.findById(userId).orElseThrow(() -> {
            log.error("NotFound. Запрос получить несуществующего пользователя с id {}.", userId);
            return new NotFoundException(
                    String.format("User with id %d is not exist.", userId)
            );
        });
        return owner.getRequests().stream()
                .sorted(Comparator.comparing(ItemRequest::getCreationDate, Comparator.reverseOrder()))
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        log.info("Получение запросов других пользователей от пользователя с id {}", userId);

        checkUser(userId);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate"));
        return itemRequestStorage.findByOwnerIdNot(userId, pageable).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
