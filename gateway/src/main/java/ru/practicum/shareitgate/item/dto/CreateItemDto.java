package ru.practicum.shareitgate.item.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@With
@Value
@Builder
public class CreateItemDto {
    Long id;
    @NotBlank(message = "Name shouldn't be blank.")
    @Size(max = 60, message = "Name's size shouldn't be more than 60 characters")
    String name;
    @NotBlank(message = "Description shouldn't be blank.")
    @Size(max = 200, message = "Description's size shouldn't be more than 200 characters")
    String description;
    @NotNull(message = "Status 'available' shouldn't be empty.")
    Boolean available;
    Long requestId;
}
