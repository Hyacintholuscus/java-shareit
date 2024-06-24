package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public Comment toComment(Long itemId,
                             User author,
                             LocalDateTime createdTime,
                             CreateCommentDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .itemId(itemId)
                .author(author)
                .created(createdTime)
                .build();
    }

    public CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
