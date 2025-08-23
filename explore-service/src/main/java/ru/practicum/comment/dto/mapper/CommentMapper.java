package ru.practicum.comment.dto.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static Comment mapToEntity(final NewCommentDto commentDto, User author, Event event) {
        return Comment.builder()
                .text(commentDto.getText())
                .author(author)
                .event(event)
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentDto mapToCommentDto(final Comment comment, Long view) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .author(UserMapper.mapToUserShortDto(comment.getAuthor()))
                .event(EventMapper.mapToShortDto(comment.getEvent(), view))
                .created(comment.getCreated())
                .build();
    }
}
