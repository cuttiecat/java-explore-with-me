package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventService {

    List<EventFullDto> getAllEventsAdmin(List<Long> users,
                                         List<EventState> states,
                                         List<Long> categories,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Integer from,
                                         Integer size);

    EventFullDto updateEventByIdAdmin(Long eventId, EventUpdateDto eventUpdatedDto);

    EventFullDto addEvent(Long userId, NewEventDto event);

    List<EventShortDto> getEventsByUser(Long userId, Pageable pageable);

    EventFullDto getEventByUser(Long userId, Long eventId);

    Event getEventById(Long id);

    EventFullDto getEventByPublic(Long eventId);

    Optional<Event> getByIdForRequest(long eventId);

    EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdateDto eventDto);

    Event save(Event event);

    List<EventShortDto> getFilteredEvents(String text, List<Long> categories, Boolean paid,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                          Boolean onlyAvailable, String sort,
                                          Integer from, Integer size);

    Collection<ParticipationRequestDto> getEventRequestsByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatus(EventRequestStatusUpdateRequest dto, Long userId, Long eventId);

}
