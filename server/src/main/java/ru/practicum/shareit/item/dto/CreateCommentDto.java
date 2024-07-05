package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@With
@Value
@Builder
@Jacksonized
public class CreateCommentDto {
    String text;
}
