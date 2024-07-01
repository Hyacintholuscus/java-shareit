package ru.practicum.shareitgate.item.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CommentDto {
    long id;
    String text;
    String authorName;
    LocalDateTime created;
}
