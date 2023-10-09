package ru.practicum.event.model;

import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Table(name = "events")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 2000)
    private String annotation;
    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;
    @Column(name = "confirmed_requests")
    private Long confirmedRequests;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(length = 7000)
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @OneToOne
    @JoinColumn(name = "initiator_id", referencedColumnName = "id")
    private User initiator;
    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;
    @Column(columnDefinition = "boolean default false")
    private Boolean paid;
    @Column(name = "participant_limit", columnDefinition = "integer default 0")
    private Integer participantLimit;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation", columnDefinition = "boolean default true")
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private EventState state;
    @Size(min = 3, max = 120)
    private String title;
    private Long views;
//    @Transient
//    private final String datePattern = Pattern.DATE;
//    @Transient
//    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

}