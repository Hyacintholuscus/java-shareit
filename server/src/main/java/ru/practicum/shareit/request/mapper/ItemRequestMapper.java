package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

@Mapper(uses = {ItemMapper.class}, componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemRequestMapper {
    @Mapping(source = "itemRequest.creationDate", target = "created")
    @Mapping(source = "itemRequest.items", target = "items", defaultExpression = "java(new ArrayList<>())")
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "ownerId", target = "ownerId")
    @Mapping(source = "creationDate", target = "creationDate")
    ItemRequest toItemRequest(Long ownerId,
                              LocalDateTime creationDate,
                              CreateItemRequestDto dto);
}
