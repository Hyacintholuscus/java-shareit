package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Size(max = 1000, message = "Text's size shouldn't be more than 1000 characters")
    private String text;
    @Column(name = "item_id")
    private Long itemId;
    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;
    @Column(name = "created_date", nullable = false)
    LocalDateTime created;
}
