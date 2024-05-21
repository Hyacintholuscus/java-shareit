package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@With
@Value
@Builder
public class ItemDto {
    Long id;
    @NotBlank(message = "Name shouldn't be empty.")
    String name;
    @NotBlank(message = "Description shouldn't be empty.")
    @Size(max = 200, message = "Description's size shouldn't be more than 200 characters")
    String description;
    @NotNull(message = "Status 'available' shouldn't be empty.")
    Boolean available;
    Integer countOfUses;
}
