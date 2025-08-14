CREATE TABLE IF NOT EXISTS users
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    name varchar(250) NOT NULL,
    email varchar(254) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);