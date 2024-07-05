package ru.practicum.shareitgate.item.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@With
@Value
@Builder
@Jacksonized
public class CreateCommentDto {
    @NotBlank
    @Size(max = 1000, message = "Text's size shouldn't be more than 1000 characters")
    String text;
}
