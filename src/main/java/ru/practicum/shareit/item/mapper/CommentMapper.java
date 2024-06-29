package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {
    @Mapping(source = "author.name", target = "authorName")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", expression = "java(null)")
    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "author", target = "author")
    @Mapping(source = "created", target = "created")
    Comment toComment(Long itemId, User author, LocalDateTime created, CreateCommentDto dto);
}
