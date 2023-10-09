package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.service.EventService;
import ru.practicum.stats_client.StatsClient;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
@Slf4j
@Validated
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    private final EventMapper eventMapper;


    @GetMapping("/{id}")
    public EventFullDto getFullInfoAboutEventById(
            @PathVariable @Min(0) Long id, HttpServletRequest request) {
        log.info("Получен запрос на получение подобной информации об опубликованном событии");
        statsClient.addHit(request.getRequestURI(), request.getRemoteAddr());
        return eventService.getEventByPublic(id);
    }

    @GetMapping
    public List<EventShortDto> getFilteredEvents(@RequestParam(required = false, defaultValue = "") String text,
                                                 @RequestParam(required = false) List<Long> categories,
                                                 @RequestParam(required = false) Boolean paid,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                 @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                 @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                 @Positive @RequestParam(defaultValue = "10") Integer size,
                                                 HttpServletRequest request) {
        log.info("Получен запрос GET на получение событий с возможностью фильтрации.");
        List<EventShortDto> dtos = eventService.getFilteredEvents(text, categories, paid,
                rangeStart,
                rangeEnd,
                onlyAvailable, sort,
                from, size);
        statsClient.addHit(request.getRequestURI(), request.getRemoteAddr());
        return dtos;
    }
}
