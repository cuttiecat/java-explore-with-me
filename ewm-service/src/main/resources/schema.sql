drop table IF EXISTS users CASCADE;
drop table IF EXISTS categories CASCADE;
drop table IF EXISTS locations CASCADE;
drop table IF EXISTS events CASCADE;
drop table IF EXISTS requests CASCADE;
drop table IF EXISTS compilations CASCADE;
drop table IF EXISTS compilation_event CASCADE;
drop table IF EXISTS comments CASCADE;


create TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name  VARCHAR NOT NULL,
    email VARCHAR NOT NULL
);

create TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL,
    CONSTRAINT uq_name_category UNIQUE (name)
);

create TABLE IF NOT EXISTS locations
(
    id  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    lat FLOAT,
    lon FLOAT
);

create TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation         VARCHAR(2000) NOT NULL,
    category_id        BIGINT,
    confirmed_requests BIGINT,
    created_on         TIMESTAMP WITHOUT TIME ZONE,
    description        VARCHAR(7000) NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE,
    initiator_id       BIGINT,
    location_id        BIGINT,
    paid               BOOLEAN,
    participant_limit  BIGINT,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    state              VARCHAR(120),
    title              VARCHAR(120),
    views              BIGINT,
    FOREIGN KEY (location_id) REFERENCES locations (id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (initiator_id) REFERENCES users (id)
);

create TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created      TIMESTAMP WITHOUT TIME ZONE,
    event_id     BIGINT,
    status       VARCHAR(55),
    requester_id BIGINT,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (requester_id) REFERENCES users (id)
);

create TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN      NOT NULL,
    title  VARCHAR(120) NOT NULL,
    CONSTRAINT uq_title_compilation UNIQUE (title)
);

create TABLE IF NOT EXISTS event_compilations
(
    compilation_id BIGINT NOT NULL REFERENCES compilations (id) ON delete CASCADE,
    event_id       BIGINT NOT NULL REFERENCES events (id) ON delete CASCADE
);

create TABLE IF NOT EXISTS comments
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    text   VARCHAR(1000)               NOT NULL,
    event_id       BIGINT,
    commentator_id BIGINT,
    published_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (commentator_id) REFERENCES users (id)
);