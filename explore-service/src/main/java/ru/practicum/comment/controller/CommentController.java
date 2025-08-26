package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // пользователь добавляет комментарий к событию.
    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@PathVariable("userId") Long userId,
                          @PathVariable("eventId") Long eventId,
                          @Valid @RequestBody NewCommentDto newComment) {
        log.info("Add comment to user {} event {}", userId, eventId);
        return commentService.create(newComment, userId, eventId);
    }

    // Пользователь получает свой конкретный комментарий
    @GetMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto get(@PathVariable("userId") Long userId,
                          @PathVariable("commentId") Long commentId) {
        log.info("Get comment for user {} event {}", userId, commentId);
        return commentService.getByUser(userId, commentId);
    }

    // Пользователь получает список своих комментариев с возможностью выборки.
    @GetMapping("/users/{userId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAll(@PathVariable("userId") Long userId,
                                   @RequestParam(value = "from", defaultValue = "0") Integer from,
                                   @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("Get all comment for user {}, from {}, size {}", userId, from, size);
        return commentService.getAllByUser(userId, PageRequest.of(from, size));
    }


    @GetMapping("/comments/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getByEvent(@PathVariable("eventId") Long eventId,
                                       @RequestParam(value = "from", defaultValue = "0") Integer from,
                                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("Get comment for event {}, from {}, size {}", eventId, from, size);
        return commentService.getAllByEvent(eventId, PageRequest.of(from, size));
    }

    // Пользователь может изменить комментарий
    @PatchMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto update(@PathVariable("userId") Long userId,
                             @PathVariable("commentId") Long commentId,
                             @Valid @RequestBody UpdateCommentDto updateComment) {
        log.info("Update comment for user {} event {}", userId, commentId);
        return commentService.update(userId, commentId, updateComment);
    }

    // Удаление коммента пользователем
    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("userId") Long userId,
                       @PathVariable("commentId") Long commentId) {
        log.info("Delete comment for user {} event {}", userId, commentId);
        commentService.delete(userId, commentId);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable("commentId") Long commentId) {
        log.info("Delete comment for admin event {}", commentId);
        commentService.delete(commentId);
    }

}
