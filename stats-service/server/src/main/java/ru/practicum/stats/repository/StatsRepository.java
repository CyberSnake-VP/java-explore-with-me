package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.EndpointHit;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

}
