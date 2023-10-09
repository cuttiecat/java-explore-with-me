package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.request.model.ConfirmedRequest;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    Collection<Request> findAllByRequesterId(long id);

    List<Request> findAllByEventId(Long eventId);

    @Query("SELECT  count(r.id) " +
            "FROM Request as r " +
            "WHERE r.event.id = :eventId " +
            "and r.status = :status")
    Optional<Long> findRequestCountByEventIdAndStatus(long eventId, RequestStatus status);

    @Query("SELECT NEW ru.practicum.request.model.ConfirmedRequest(r.event.id,COUNT(DISTINCT r)) " +
            "FROM Request r " +
            "WHERE r.status = 'CONFIRMED' AND r.event.id IN :eventsIds " +
            "GROUP BY r.event.id")
    List<ConfirmedRequest> findConfirmedRequest(@Param(value = "eventsIds") List<Long> eventsIds);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByIdInAndStatus(List<Long> id, RequestStatus status);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

}
