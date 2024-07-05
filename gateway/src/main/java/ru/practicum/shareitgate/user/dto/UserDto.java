package ru.practicum.shareitgate.user.dto;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@With
@Value
@Builder
public class UserDto {
    Long id;
    @NotBlank(message = "User's name shouldn't be empty.")
    @Size(max = 200, message = "Name's size shouldn't be more than 200 characters")
    String name;
    @Email(message = "Email must be in email address format.")
    @NotBlank(message = "Email shouldn't be empty.")
    @Size(max = 200, message = "Email's size shouldn't be more than 200 characters")
    String email;
}
