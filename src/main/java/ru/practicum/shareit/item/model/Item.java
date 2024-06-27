package ru.practicum.shareit.item.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Generated
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
    @OneToMany(mappedBy = "itemId")
    private List<Comment> comments;
    @Column(name = "request_id")
    private Long requestId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        if (id == null) return false;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", available=" + available +
                ", ownerId=" + ownerId +
                '}';
    }
}
