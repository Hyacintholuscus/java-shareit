package ru.practicum.shareitgate.request.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareitgate.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ItemRequestDto {
    Long id;
    String description;
    LocalDateTime created;
    List<ItemDto> items;
}
