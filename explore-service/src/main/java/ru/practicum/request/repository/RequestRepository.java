package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Request findRequestByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findRequestByRequesterId(Long userId);
}
