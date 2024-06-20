package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequestDto toDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreationDate())
                .items(itemRequest.getItems().stream()
                        .map(itemMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public ItemRequest toItemRequest(Long ownerId,
                                     LocalDateTime creationDate,
                                     CreateItemRequestDto dto) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .creationDate(creationDate)
                .ownerId(ownerId)
                .build();
    }
}
