package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final EventService eventService;

    @Transactional
    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Добавление нового комментария, userId = {}, eventId = {}, ", userId,eventId);
        log.debug("newCommentDto = {}", newCommentDto);
        User user = userService.checkUserExistAndGet(userId);
        Event event = eventService.getEventById(eventId);
        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setEvent(event);
        comment.setCommentator(user);
        comment.setPublishedOn(LocalDateTime.now());
        var result = commentMapper.toCommentDto(commentRepository.save(comment));
        log.info("Новый комментарий добавлен, id = {}, userId = {}, eventId = {}", result.getId(), userId, eventId);
        return result;
    }

    @Transactional
    @Override
    public CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto newCommentDto) {
        Comment oldComment = checkCommentExistAndGet(commentId);
        userService.checkUserExistAndGet(userId);
        if (!oldComment.getCommentator().getId().equals(userId)) {
            //409 CONFLICT
            throw new AccessDeniedException("Невозможно обновить комментарий, т.к. пользователь не является его автором.");
        }
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentRepository.save(oldComment);
        log.info("Комментарий с id = {} обновлён.", commentId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Transactional
    @Override
    public List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size) {
        log.info("Получен GET-запрос на получение всех комментариев для события с id = {}", eventId);
        log.debug("eventId = {}", eventId);
        eventService.getEventById(eventId);
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> eventComments = commentRepository.findAllByEventId(eventId, page);
        log.info("GET-запрос на получение всех комментариев для события с id = {} успешно выполнен. from = {}, size = {}.", eventId, from, size);
        return commentMapper.toCommentDtoList(eventComments);
    }

    @Transactional
    @Override
    public CommentDto getCommentById(Long commentId) {
        log.info("Получение комментария по id: {}", commentId);
        log.debug("commentId = {}", commentId);
        Comment comment = checkCommentExistAndGet(commentId);
        log.info("Получение комментария по id успешно выполнено. 'commentId = {}'", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Transactional
    @Override
    public void deleteCommentById(Long commentId) {
        log.info("Получен запрос DELETE на удаление комментария по id = {} администратором.", commentId);
        log.debug("сommentId = {}", commentId);
        Comment comment = checkCommentExistAndGet(commentId);
        commentRepository.delete(comment);
        log.info("Комментарий с id = {} успешно удалён администратором.", commentId);
    }

    @Transactional
    @Override
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = checkCommentExistAndGet(commentId);
        userService.checkUserExistAndGet(userId);
        if (!comment.getCommentator().getId().equals(userId)) {
            throw new AccessDeniedException("Невозможно удалить комментарий, т.к. пользователь не является его автором.");
        }
        log.info("Комментрарий с id = {} обновлён.", commentId);
        commentRepository.delete(comment);
    }

    private Comment checkCommentExistAndGet(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий с id " + commentId + " не найден"));
    }

}
