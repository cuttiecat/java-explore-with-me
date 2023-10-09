package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Slf4j
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(name = "from", defaultValue = "0") Integer from,
                                               @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен запрос GET на получение событий, добавленных текущим пользователем");
        return eventService.getEventsByUser(userId, PageRequest.of(from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Получен запрос POST на добавление нового события");
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUser(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получен запрос GET получение полной информации о событии добавленном текущим пользователем");
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "eventId") Long eventId,
                                    @Valid @RequestBody EventUpdateDto eventDto) {
        log.info("Получен запрос PATCH на изменение собатия добавленного текущим пользователем");
        return eventService.updateEventByIdPrivate(userId, eventId, eventDto);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getEventRequestsByUser(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получен запрос GET на получение информации о запросах на участие в событии текущего пользователя");
        return eventService.getEventRequestsByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody EventRequestStatusUpdateRequest dto) {

        if (dto == null) {
            throw new AccessDeniedException("Нет изменяемых данных");
        }
        log.info("Получен запрос PATCH на изменение статуса заявок на участие в событии");
        return eventService.updateEventRequestStatus(dto, userId, eventId);
    }

}

