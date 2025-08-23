DROP TABLE IF EXISTS events CASCADE ;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS compilations_events CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    name varchar(250) NOT NULL,
    email varchar(254) NOT NULL UNIQUE ,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS categories
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) UNIQUE,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    annotation TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    confirmed_request BIGINT,
    created_on TIMESTAMP WITHOUT TIME ZONE,
    description TEXT NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER,
    request_moderation BOOLEAN,
    title VARCHAR(120) NOT NULL,
    state VARCHAR(50) NOT NULL,
    initiator_id BIGINT NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_events PRIMARY KEY (id),
    FOREIGN KEY (initiator_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    event_id BIGINT NOT NULL,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    requester_id BIGINT NOT NULL,
    status VARCHAR(100)NOT NULL,
    CONSTRAINT pk_requests PRIMARY KEY (id),
    FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS compilations
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    pinned BOOLEAN NOT NULL,
    title VARCHAR(50)NOT NULL,
    CONSTRAINT pk_compilations PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS compilations_events
(
    event_id BIGINT NOT NULL,
    compilation_id BIGINT NOT NULL,
    CONSTRAINT pk_compilations_events PRIMARY KEY (event_id, compilation_id),
    FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    FOREIGN KEY (compilation_id) REFERENCES compilations (id) ON DELETE CASCADE
);