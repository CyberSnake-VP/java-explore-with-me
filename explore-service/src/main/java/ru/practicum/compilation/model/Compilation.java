package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;

import java.util.List;

@Entity
@Table(name = "compilations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean pinned;
    private String title;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> events;
}
