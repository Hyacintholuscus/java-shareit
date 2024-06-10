package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@With
@Value
@Builder
public class UserDto {
    Long id;
    @NotBlank(message = "User's name shouldn't be empty.")
    String name;
    @Email(message = "Email must be in email address format.")
    @NotBlank(message = "Email shouldn't be empty.")
    String email;
}
