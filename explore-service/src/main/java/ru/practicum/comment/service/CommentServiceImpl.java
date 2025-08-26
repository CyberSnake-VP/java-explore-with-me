package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.utils.StatsClientUtil;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.dto.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final StatsClientUtil statsClient;

    @Transactional
    @Override
    public CommentDto create(NewCommentDto newComment, Long userId, Long eventId) {
        log.info("Create new comment {}", newComment);
        User userEntity = userRepository.findById(userId).orElseThrow(() -> getNotFoundException(userId, "User"));
        Event eventEntity = eventRepository.findById(eventId).orElseThrow(() -> getNotFoundException(eventId, "Event"));
        Comment commentEntity = CommentMapper.mapToEntity(newComment, userEntity, eventEntity);
        log.info("Comment {}, created", newComment.getText());
        commentRepository.save(commentEntity);
        return CommentMapper.mapToCommentDto(commentEntity, statsClient.getEventHitView(eventEntity));
    }

    @Override
    public CommentDto getByUser(Long userId, Long commentId) {
        log.info("Get comment by user {}", userId);
        Comment entity = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> getNotFoundException(commentId, "Comment"));
        log.info("Comment found: {}", entity);
        return CommentMapper.mapToCommentDto(entity, statsClient.getEventHitView(entity.getEvent()));
    }

    @Override
    public List<CommentDto> getAllByUser(Long userId, Pageable pageable) {
        log.info("Get all comment by user {}", userId);

        // получим список комментариев
        List<Comment> entitysList = commentRepository.findAll(pageable).getContent();
        // извлечем из списка комментов события к которым писались комментарии
        List<Event> eventsEntity = entitysList.stream().map(Comment::getEvent).toList();
        // получим статистику посещений для списка событий одним запросом.
        List<EventShortDto> eventsShort = statsClient.getEventShortDto(eventsEntity);
        return CommentMapper.mapToCommentDto(entitysList, new ArrayList<>(eventsShort));
    }

    @Override
    public List<CommentDto> getAllByEvent(Long eventId, Pageable pageable) {
        log.info("Get all comment by event {}", eventId);

        // получим список комментариев к конкретному событию
        List<Comment> entitysList = commentRepository.findAllByEventId(eventId, pageable);
        // извлечем из списка комментов события к которым писались комментарии
        List<Event> eventsEntity = entitysList.stream().map(Comment::getEvent).toList();
        // получим статистику посещений для списка событий одним запросом.
        List<EventShortDto> eventsShort = statsClient.getEventShortDto(eventsEntity);
        return CommentMapper.mapToCommentDto(entitysList, new ArrayList<>(eventsShort));
    }

    @Transactional
    @Override
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto updateComment) {
        log.info("Update comment {}, for user: {}", updateComment, userId);
        // ищем пользователя
        userRepository.findById(userId).orElseThrow(() -> getNotFoundException(userId, "User"));
        // ищем нужный комментарий для обновления
        Comment commentEntity = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> getNotFoundException(commentId, "Comment"));

        commentEntity.setText(updateComment.getText());

        log.info("Comment {}, updated", updateComment);
        commentEntity = commentRepository.save(commentEntity);
        return CommentMapper.mapToCommentDto(commentEntity, statsClient.getEventHitView(commentEntity.getEvent()));
    }

    @Transactional
    @Override
    public void delete(Long userId, Long commentId) {
        log.info("Delete comment {}, for user: {}", commentId, userId);
        // после проверок на существование
        Comment entity = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> getNotFoundException(commentId, "Comment"));
        log.info("Comment deleted: {}", entity);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void delete(Long commentId) {
        log.info("Delete comment {} by Admin", commentId);
        Comment entity = commentRepository.findById(commentId)
                .orElseThrow(() -> getNotFoundException(commentId, "Comment"));
        log.info("Comment deleted by Admin: {}", entity);
        commentRepository.deleteById(commentId);
    }

    private NotFoundException getNotFoundException(Long id, String nameEntity) {
        log.info("{} not found with id: {}", nameEntity, id);
        String reason = "The required object was not found.";
        String message = String.format("%s with id=%d was not found", nameEntity, id);
        return new NotFoundException(message, reason);
    }
}
