package ru.practicum.comment.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(NewCommentDto newComment, Long userId, Long eventId);
    CommentDto getByUser(Long userId, Long commentId);
    List<CommentDto> getAllByUser(Long userId, Pageable pageable);
    List<CommentDto> getAllByEvent(Long eventId, Pageable pageable);
    CommentDto update(Long userId, Long commentId, UpdateCommentDto updateComment);
    void delete(Long userId, Long commentId);
    void delete(Long commentId);
}
