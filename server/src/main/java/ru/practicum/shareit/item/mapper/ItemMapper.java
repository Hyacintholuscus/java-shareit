package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;

@Mapper(uses = {CommentMapper.class}, componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {ArrayList.class})
public interface ItemMapper {
    @Mapping(source = "item.comments", target = "comments", defaultExpression = "java(new ArrayList<>())")
    ItemDto toDto(Item item);

    @Mapping(source = "item.id", target = "id")
    @Mapping(source = "item.comments", target = "comments", defaultExpression = "java(new ArrayList<>())")
    @Mapping(source = "nextBooking", target = "nextBooking")
    @Mapping(source = "lastBooking", target = "lastBooking")
    ItemDto toDto(Item item, BookingItemDto lastBooking, BookingItemDto nextBooking);

    @Mapping(source = "ownerId", target = "ownerId")
    Item toItem(Long ownerId, ItemDto dto);
}
