package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@With
@Value
@Builder
public class UserDto {
    Long id;
    String name;
    String email;
}
