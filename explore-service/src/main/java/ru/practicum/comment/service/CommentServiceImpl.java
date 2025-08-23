package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.dto.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
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
    private final StatsClient statsClient;

    @Transactional
    @Override
    public CommentDto create(NewCommentDto newComment, Long userId, Long eventId) {
        log.info("Create new comment {}", newComment);
        User userEntity = userRepository.findById(userId).orElseThrow(() -> getNotFoundException(userId, "User"));
        Event eventEntity = eventRepository.findById(eventId).orElseThrow(() -> getNotFoundException(eventId, "Event"));
        Comment commentEntity = CommentMapper.mapToEntity(newComment, userEntity, eventEntity);
        log.info("Comment {}, created", newComment.getText());
        commentRepository.save(commentEntity);
        return CommentMapper.mapToCommentDto(commentEntity, getEventHitView(eventEntity));
    }

    @Override
    public CommentDto getByUser(Long userId, Long commentId) {
        log.info("Get comment by user {}", userId);
        Comment entity = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> getNotFoundException(commentId, "Comment"));
        log.info("Comment found: {}", entity);
        return CommentMapper.mapToCommentDto(entity, getEventHitView(entity.getEvent()));
    }

    @Override
    public List<CommentDto> getAllByUser(Long userId, Pageable pageable) {
        log.info("Get all comment by user {}", userId);
        List<Comment> entitysList = commentRepository.findAll(pageable).getContent();
        return entitysList.stream()
                .map(c -> CommentMapper.mapToCommentDto(c, getEventHitView(c.getEvent())))
                .toList();
    }

    @Override
    public List<CommentDto> getAllByEvent(Long eventId, Pageable pageable) {
        log.info("Get all comment by event {}", eventId);
        List<Comment> entitysList = commentRepository.findAllByEventId(eventId, pageable);
        return entitysList.stream()
                .map(c -> CommentMapper.mapToCommentDto(c, getEventHitView(c.getEvent())))
                .toList();
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
        return CommentMapper.mapToCommentDto(commentEntity, getEventHitView(commentEntity.getEvent()));
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

    // Метод для получения кол-ва просмотров из сервиса статистики.
    // Не понятно за какой период получать статистику, указал за 365 дней.
    private Long getEventHitView(Event event) {
        // получим выборку в один год от текущей даты.
        LocalDateTime start = LocalDateTime.now().minusDays(365);
        LocalDateTime end = LocalDateTime.now();
        Long eventId = event.getId();
        List<String> uris = new ArrayList<>();
        uris.add("/events/" + eventId);
        List<ViewStatsDto> views = statsClient.getStats(start, end, uris, true);
        Long view = 0L;
        if (!views.isEmpty()) {
            return views.getFirst().getHits();
        }
        return view;
    }
}
