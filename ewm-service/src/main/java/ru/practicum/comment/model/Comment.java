package ru.practicum.comment.model;

import lombok.*;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "comments")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "text", nullable = false)
    private String text;
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @ManyToOne(optional = false)
    @JoinColumn(name = "commentator_id", nullable = false)
    private User commentator;
    @Column(name = "published_on", nullable = false)
    private LocalDateTime publishedOn;
}
