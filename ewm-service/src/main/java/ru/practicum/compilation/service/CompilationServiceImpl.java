package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.util.Pagination;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationMapper compilationMapper;

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        Set<Long> eventsId = newCompilationDto.getEvents();
        if (eventsId != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventsId));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.mapToCompilationDto(savedCompilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilation) {
        Compilation compilationFromDb = checkCompilationExistAndGet(compId);
        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            Set<Long> eventIds = compilation.getEvents();
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventIds));
            compilationFromDb.setEvents(events);
        }
        if (compilation.getPinned() != null) {
            compilationFromDb.setPinned(compilation.getPinned());
        }
        if (compilation.getTitle() != null) {
            compilationFromDb.setTitle(compilation.getTitle());
        }
        Compilation updated = compilationRepository.save(compilationFromDb);
        return compilationMapper.mapToCompilationDto(updated);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получены все подборки");
        if (pinned == null) {
            return compilationRepository.findAll(new Pagination(from, size, Sort.unsorted())).getContent().stream()
                    .map(compilationMapper::mapToCompilationDto)
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAllByPinned(pinned, new Pagination(from, size, Sort.unsorted()))
                .getContent().stream()
                .map(compilationMapper::mapToCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilation(Long id) {
        Compilation compilation = checkCompilationExistAndGet(id);
        return compilationMapper.mapToCompilationDto(compilation);
    }


    @Transactional
    @Override
    public Compilation checkCompilationExistAndGet(Long id) {
        return compilationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Подборки с id " + id + " не найдено"));
    }

}




