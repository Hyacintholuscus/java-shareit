package ru.practicum.shareit.item.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@With
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name shouldn't be empty.")
    @Size(max = 60, message = "Name's size shouldn't be more than 60 characters")
    private String name;
    @NotBlank(message = "Description shouldn't be empty.")
    @Size(max = 200, message = "Description's size shouldn't be more than 200 characters")
    private String description;
    @NotNull(message = "Status 'available' shouldn't be empty.")
    private Boolean available;
    @Column(name = "owner_id")
    private Long ownerId;
    @Transient
    private List<Long> tenantIds; // TODO: Возможно, убрать. Пользователи, которые брали вещь в аренду
}
