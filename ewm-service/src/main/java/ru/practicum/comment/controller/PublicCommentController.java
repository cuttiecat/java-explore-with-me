package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Slf4j
@Validated
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping("/{eventId}")
    public List<CommentDto> getCommentsByEventId(@PathVariable Long eventId,
                                                 @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return commentService.getCommentsByEventId(eventId, from, size);
    }

}
