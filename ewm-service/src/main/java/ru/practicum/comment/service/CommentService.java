package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto newCommentDto);

    List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size);

    void deleteCommentById(Long commentId);

    void deleteCommentByUser(Long userId, Long commentId);

    CommentDto getCommentById(Long commentId);

}
