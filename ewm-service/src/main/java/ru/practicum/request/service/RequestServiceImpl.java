package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;


    @Override
    public Request createRequest(Long userId, Long eventId) {
        User user = userService.checkUserExistAndGet(userId);
        Event event = eventService.getByIdForRequest(eventId).orElseThrow(
                () -> new AccessDeniedException("Недопустимый запрос"));

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new AccessDeniedException("Вы не можете создать один и тот же запрос дважды");
        }

        if (userId.equals(event.getInitiator().getId())) {
            throw new AccessDeniedException("Инициатор мероприятия не может участвовать в собственном мероприятии");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new AccessDeniedException("Участие невозможно, если событие не опубликовано");
        }
        if (event.getParticipantLimit() > 0) {
            Long participants = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            Integer limit = event.getParticipantLimit();
            if (participants >= limit) {
                throw new AccessDeniedException("Лимит участников исчерпан.");
            }
        }
        Request newRequest = Request.builder()
                .created(LocalDateTime.now())
                .requester(user)
                .event(event)
                .status(RequestStatus.PENDING)
                .build();

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        }

        newRequest = requestRepository.save(newRequest);

        if (newRequest.getStatus() == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventService.save(event);
        }

        return newRequest;
    }

    @Override
    public Request cancelRequest(Long userId, Long requestId) {
        userService.checkUserExistAndGet(userId);
        Request request = checkRequestExistAndGet(requestId);
        request.setStatus(RequestStatus.CANCELED);
        return requestRepository.save(request);
    }

    @Override
    public Collection<Request> getUserRequests(Long userId) {
        userService.checkUserExistAndGet(userId);
        return requestRepository.findAllByRequesterId(userId);
    }

    private Request checkRequestExistAndGet(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос с id " + requestId + " не найден"));
    }
}
