package ru.practicum.stats_server.mapper;

import ru.practicum.stats_dto.EndpointHitDto;
import ru.practicum.stats_server.model.Stats;

public class StatsMapper {
    public static Stats fromEndpointHitDto(EndpointHitDto endpointHitDto) {
        return Stats.builder()
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(endpointHitDto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toEndpointHitDto(Stats stats) {
        return EndpointHitDto.builder()
                .app(stats.getApp())
                .uri(stats.getUri())
                .ip(stats.getIp())
                .timestamp(stats.getTimestamp())
                .build();
    }
}

