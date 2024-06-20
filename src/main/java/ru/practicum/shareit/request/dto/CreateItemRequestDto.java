package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
@Builder
public class CreateItemRequestDto {
    @NotBlank(message = "Description shouldn't be blank.")
    @Size(max = 300, message = "Description's size shouldn't be more than 300 characters")
    String description;
}
