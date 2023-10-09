package ru.practicum.stats_server.service;

import ru.practicum.stats_dto.EndpointHitDto;
import ru.practicum.stats_dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto addHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique);
}
