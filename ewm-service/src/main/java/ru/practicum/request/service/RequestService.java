package ru.practicum.request.service;

import ru.practicum.request.model.Request;

import java.util.Collection;

public interface RequestService {

    Request createRequest(Long userId, Long eventId);

    Request cancelRequest(Long userId, Long requestId);

    Collection<Request> getUserRequests(Long userId);

}
