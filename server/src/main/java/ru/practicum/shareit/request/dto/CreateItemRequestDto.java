package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@With
@Value
@Builder
@Jacksonized
public class CreateItemRequestDto {
    String description;
}
