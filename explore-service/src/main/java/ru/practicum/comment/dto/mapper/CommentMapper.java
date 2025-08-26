package ru.practicum.comment.dto.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.dto.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static CommentDto mapToCommentDto(final Comment comment, EventShortDto event) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .author(UserMapper.mapToUserShortDto(comment.getAuthor()))
                .event(event)
                .created(comment.getCreated())
                .build();
    }

    public static List<CommentDto> mapToCommentDto( List<Comment> comments, ArrayList<EventShortDto> events) {
        List<CommentDto> commentDtos = new ArrayList<>();

        /** Пробегаемся по списку комментариев и списку событий. У комментария одно событие, значит в моменте перебора событий
         * добавим в итоговый список commentDtos результат маппинга, для текущего коммента и первого в списке при переборе события
         * т.к. событие уже будет добавлено к комментарию, удалим его из списка и пропустим итерацию следующих событий.
         * Для следующего комментария проделываем тоже самое, тогда первое событие в списке событий уже будет его.*/
        for (Comment comment : comments) {
            for (EventShortDto eventShort : events) {
                commentDtos.add(mapToCommentDto(comment,eventShort));
                events.remove(eventShort);
                break;
            }
        }
        return commentDtos;
    }
}
