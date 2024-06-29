package ru.practicum.shareit.booking.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@Mapper(uses = {UserMapper.class, ItemMapper.class}, componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "booking.startDate", target = "start")
    @Mapping(source = "booking.endDate", target = "end")
    BookingDto toDto(Booking booking);

    @Mapping(source = "booking.startDate", target = "start")
    @Mapping(source = "booking.endDate", target = "end")
    @Mapping(source = "booker.id", target = "bookerId")
    BookingItemDto toItemDto(Booking booking);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "booker", target = "booker")
    @Mapping(source = "item", target = "item")
    @Mapping(source = "dto.start", target = "startDate")
    @Mapping(source = "dto.end", target = "endDate")
    @Mapping(target = "status", constant = "WAITING")
    Booking toBooking(CreateBookingDto dto, User booker, Item item);
}
