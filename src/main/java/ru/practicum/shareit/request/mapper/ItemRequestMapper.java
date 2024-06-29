package ru.practicum.shareit.request.mapper;

import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Mapper(uses = {ItemMapper.class}, componentModel = "spring")
public interface ItemRequestMapper {
    @BeforeMapping
    default void validate(ItemRequest itemRequest) {
        if (itemRequest.getItems() == null) itemRequest.setItems(new ArrayList<>());
    }

    @Mapping(source = "itemRequest.creationDate", target = "created")
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "ownerId", target = "ownerId")
    @Mapping(source = "creationDate", target = "creationDate")
    ItemRequest toItemRequest(Long ownerId,
                              LocalDateTime creationDate,
                              CreateItemRequestDto dto);
}
