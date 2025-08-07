package ru.practicum;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String app;

    private String uri;

    private String ip;

    @Column(name = "timestamp_value")
    private LocalDateTime timestamp;
}
