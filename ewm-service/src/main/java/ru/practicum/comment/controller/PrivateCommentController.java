package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
@Slf4j
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestBody @Valid NewCommentDto newComment,
                                 @PathVariable("userId") Long userId,
                                 @PathVariable("eventId") Long eventId) {
        log.info("Получен запрос POST на добавление нового комментария к событию.");
        return commentService.addComment(userId, eventId, newComment);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentByUser(@RequestBody @Valid NewCommentDto newCommentDto,
                                          @PathVariable("userId") Long userId,
                                          @PathVariable("commentId") Long commentId) {
        log.info("Получен запрос PATCH на редактирование комментария пользователем.");
        return commentService.updateCommentByUser(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable("userId") Long userId,
                                    @PathVariable("commentId") Long commentId) {
        log.info("Получен запрос DELETE на удаление комментария пользователем.");
        commentService.deleteCommentByUser(userId, commentId);
    }
}
