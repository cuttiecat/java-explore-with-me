package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.EventStateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ConfirmedRequest;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stats_client.StatsClient;
import ru.practicum.stats_dto.ViewStatsDto;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;
import ru.practicum.util.Pagination;

import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.request.model.RequestStatus.CONFIRMED;
import static ru.practicum.request.model.RequestStatus.REJECTED;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UserService userService;

    private final CategoryService categoryService;
    private final EventMapper eventMapper;

    private final RequestMapper requestMapper;

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    private final LocationRepository locationRepository;

    private final RequestRepository requestRepository;

    private final StatsClient statsClient;


    @Override
    public List<EventFullDto> getAllEventsAdmin(List<Long> users,
                                                List<EventState> states,
                                                List<Long> categories,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                Integer from,
                                                Integer size) {

        validDateParam(rangeStart, rangeEnd);
        PageRequest pageable = new Pagination(from, size, Sort.unsorted());
        List<Event> events = eventRepository.findAllForAdmin(users, states, categories, getRangeStart(rangeStart),
                pageable);
        getConfirmedRequests(events);
        log.info("Get all events in admin {}", events);
        return events.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByIdAdmin(Long eventId, EventUpdateDto eventUpdatedDto) {
        Event event = getEventById(eventId);
        updateEventAdmin(event, eventUpdatedDto);
        event = eventRepository.save(event);
        locationRepository.save(event.getLocation());
        log.info("Update event with id= {} in admin ", eventId);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventByIdPrivate(Long userId, Long eventId, EventUpdateDto eventDto) {
        Event event = getEventByIdAndInitiatorId(eventId, userId);
        if (event.getState() == EventState.PUBLISHED || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new AccessDeniedException("События, CANCELED или PENDING своего завершения, могут быть обновлены");
        }
        updateEvent(event, eventDto);
        Event eventSaved = eventRepository.save(event);
        locationRepository.save(eventSaved.getLocation());
        log.info("Обновление события с id={} для пользователя id= {} in private", eventId, userId);
        return eventMapper.toEventFullDto(eventSaved);
    }

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto event) {
        User user = userService.checkUserExistAndGet(userId);

        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        validateTime(event.getEventDate());
        Event eventToSave = eventMapper.toEventModel(event);
        eventToSave.setState(EventState.PENDING);
        eventToSave.setConfirmedRequests(0L);
        eventToSave.setCreatedOn(LocalDateTime.now());

        Category category = categoryService.checkCategoryExistAndGet(event.getCategory());
        eventToSave.setCategory(category);
        eventToSave.setInitiator(user);
        Event saved = eventRepository.save(eventToSave);
        return eventMapper.toEventFullDto(saved);
    }

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Pageable pageable) {
        return eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, pageable).toList());
    }

    @Transactional
    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        return eventMapper.toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Такого события не существует.")));
    }

    @Override
    public List<EventShortDto> getFilteredEvents(String text, List<Long> categories, Boolean paid,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Boolean onlyAvailable, String sort,
                                                 Integer from, Integer size) {
        validDateParam(rangeStart, rangeEnd);
        LocalDateTime dateStartSearch = LocalDateTime.now().plusSeconds(1L);
        LocalDateTime dateEndSearch = LocalDateTime.now().plusYears(99L);
        if (rangeStart != null) {
            dateStartSearch = rangeStart;
        }
        if (rangeEnd != null) {
            dateEndSearch = rangeEnd;
        }
        if (categories == null || categories.size() == 0) {
            categories = categoryRepository.findAll().stream()
                    .map(c -> c.getId())
                    .collect(Collectors.toList());
        }
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.searchEventPub(text, categories, paid, dateStartSearch, dateEndSearch, EventState.PUBLISHED, pageable);
        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() > getConfirmedRequests(e.getId()))
                    .collect(Collectors.toList());
        }
        LocalDateTime start = dateStartSearch;
        LocalDateTime end = dateEndSearch;
        List<EventShortDto> eventShorts = events.stream()
                .map(eventMapper::toEventShortDto)
                .peek(e -> {

                    e.setViews(viewsEvent(start, end, "/events/" + e.getId(), false));
                })
                .collect(Collectors.toList());
        if (sort.equals("VIEWS")) {
            eventShorts.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews));
        }
        return eventShorts;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatus(EventRequestStatusUpdateRequest dto, Long userId, Long eventId) {
        if (dto.getStatus() == null || dto.getRequestIds() == null) {
            throw new AccessDeniedException("Нет статуса или идентификаторов для замены");
        }
        userService.checkUserExistAndGet(userId);
        Event event = getEventById(eventId);
        List<Long> requestsId = dto.getRequestIds();
        List<Request> requests = requestRepository.findAllById(requestsId);
        for (Request request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new AccessDeniedException("Статус заявки остается неизменным");
            }
            if (dto.getStatus() == CONFIRMED) {
                if (event.getParticipantLimit() <= getConfirmedRequests(event.getId())) {
                    throw new AccessDeniedException("Лимит участников истек");
                } else {
                    request.setStatus(CONFIRMED);
                }
            } else if (dto.getStatus() == REJECTED) {
                request.setStatus(REJECTED);
            }
        }
        List<ParticipationRequestDto> confirmedRequests = requestRepository.findAllByIdInAndStatus(requestsId,
                CONFIRMED).stream().map(requestMapper::toParticipationRequestDto).collect(Collectors.toList());
        List<ParticipationRequestDto> rejectedRequests = requestRepository.findAllByIdInAndStatus(requestsId,
                REJECTED).stream().map(requestMapper::toParticipationRequestDto).collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public Optional<Event> getByIdForRequest(long eventId) {
        return eventRepository.findById(eventId);
    }

    @Transactional
    public Collection<ParticipationRequestDto> getEventRequestsByUser(Long userId, Long eventId) {
        if (eventRepository.findByIdAndInitiatorId(eventId, userId).isPresent()) {
            return requestRepository.findAllByEventId(eventId).stream()
                    .map(requestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList());
        }
        log.info("Получена заявка на участие в мероприятии с помощью id ={}", eventId);
        return Collections.emptyList();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + id + " не найдено"));
    }

    @Override
    public EventFullDto getEventByPublic(Long eventId) {
        Event event = getEventById(eventId);
        boolean published = (event.getState() == EventState.PUBLISHED);
        if (!published) {
            throw new NotFoundException("Событие с id = " + eventId + " не опубликовано.");
        }
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Long confirmedRequests = getConfirmedRequests(event.getId());
        Long views = viewsEvent(LocalDateTime.now().plusSeconds(1L), LocalDateTime.now().plusYears(99L), "/events/" + event.getId(), false);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    private void getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<ConfirmedRequest> confirmedRequests = requestRepository.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }

    private Event getEventByIdAndInitiatorId(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private void updateEvent(Event event, EventUpdateDto eventDto) {
        updateEventCommonFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
            if (eventDto.getStateAction().equals(EventStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
        }
    }

    private Long viewsEvent(LocalDateTime rangeStart, LocalDateTime rangeEnd, String uris, Boolean unique) {
        ResponseEntity<Object> response = statsClient.getStats(rangeStart, rangeEnd, List.of(uris), unique);
        Object body = response.getBody();

        if (body != null && body instanceof List<?>) {
            List<?> dto = (List<?>) body;
            return dto.size() > 0 ? ((ViewStatsDto) dto.get(0)).getHits() : 1L;
        } else {
            return 0L;
        }
    }

    private Category getCategoryForEvent(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + id + " не найдена"));
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
    }

    private void validateTime(LocalDateTime start) {
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала события должна быть не ранее чем за час от даты публикации");
        }
    }

    private void validDateParam(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new ValidationException("Дата начала не может быть указана после даты окончания");
            }
        }
    }

    private LocalDateTime getRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) {
            return LocalDateTime.now();
        }
        return rangeStart;
    }

    private void updateEventAdmin(Event event, EventUpdateDto eventDto) {
        updateEventCommonFields(event, eventDto);

        if (eventDto.getStateAction() != null) {
            if (event.getState().equals(EventState.PENDING)) {
                if (eventDto.getStateAction().equals(EventStateAction.REJECT_EVENT)) {
                    event.setState(EventState.CANCELED);
                }
                if (eventDto.getStateAction().equals(EventStateAction.PUBLISH_EVENT)) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
            } else {
                throw new AccessDeniedException("Не удается опубликовать или отменить событие, поскольку оно находится в неправильном состоянии"
                        + event.getState());
            }
        }

        if (eventDto.getEventDate() != null && event.getState().equals(EventState.PUBLISHED)) {
            if (eventDto.getEventDate().isAfter(event.getPublishedOn().plusHours(1))) {
                event.setEventDate(eventDto.getEventDate());
            } else {
                throw new AccessDeniedException("Дата начала события должна быть не ранее чем за час от даты публикации.");
            }
        }
    }

    private void updateEventCommonFields(Event event, EventUpdateDto eventDto) {
        if (eventDto.getAnnotation() != null && !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getDescription() != null && !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getCategory() != null) {
            Category category = getCategoryForEvent(eventDto.getCategory());
            event.setCategory(category);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null && !eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }
        if (eventDto.getEventDate() != null) {
            validateTime(eventDto.getEventDate());
            event.setEventDate(eventDto.getEventDate());
        }
    }

}