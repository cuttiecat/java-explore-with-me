package ru.practicum.stats_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats_dto.EndpointHitDto;
import ru.practicum.stats_dto.ViewStatsDto;
import ru.practicum.stats_server.exception.BadRequestException;
import ru.practicum.stats_server.model.Stats;
import ru.practicum.stats_server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.stats_server.mapper.StatsMapper.fromEndpointHitDto;
import static ru.practicum.stats_server.mapper.StatsMapper.toEndpointHitDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto addHit(EndpointHitDto endpointHitDto) {
        Stats stats = statsRepository.save(fromEndpointHitDto(endpointHitDto));
        log.info("Statistic добавлена: {}", stats);
        return toEndpointHitDto(stats);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {
        validateDateOrder(start, end);
        if (unique) {
            if (uris == null) {
                return statsRepository.getStatsByUniqueWithoutUri(start, end);
            }
            return statsRepository.getStatsByUnique(start, end, uris);
        } else {
            if (uris == null) {
                return statsRepository.getStatsByUriWithoutUnique(start, end);
            }
            return statsRepository.getAllStats(start, end, uris);
        }
    }

    private void validateDateOrder(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new BadRequestException("Дата окончания не может быть перед датой начала.");
        }
    }
}
