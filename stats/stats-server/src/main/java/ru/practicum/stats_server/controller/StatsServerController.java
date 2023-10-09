package ru.practicum.stats_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats_dto.EndpointHitDto;
import ru.practicum.stats_dto.ViewStatsDto;
import ru.practicum.stats_server.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsServerController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<EndpointHitDto> addHit(@RequestBody @Valid EndpointHitDto endpointHitDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Hit не сохранён: {}", endpointHitDto);
            return ResponseEntity
                    .badRequest()
                    .body(endpointHitDto);
        }

        log.info("Hit сохранён: {}", endpointHitDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(statsService.addHit(endpointHitDto));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                       LocalDateTime start,
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                       LocalDateTime end,
                                                       @RequestParam(name = "uris", required = false) String[] uris,
                                                       @RequestParam(name = "unique", defaultValue = "false")
                                                       boolean unique) {

        log.info("Статистика получена");
        return ResponseEntity.ok(statsService.getStats(start, end, uris, unique));
    }

}
