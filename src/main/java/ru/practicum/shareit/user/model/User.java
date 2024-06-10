package ru.practicum.shareit.user.model;

import lombok.*;

import ru.practicum.shareit.item.model.Item;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@With
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "User's name shouldn't be empty.")
    private String name;
    @Column(name = "email", nullable = false, unique = true)
    @Email(message = "Email must be in email address format.")
    @NotBlank(message = "Email shouldn't be empty.")
    private String email;
    @OneToMany(mappedBy = "owner")
    private List<Item> items;
}
