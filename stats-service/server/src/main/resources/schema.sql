CREATE TABLE IF NOT EXISTS stats
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    app varchar(25) NOT NULL,
    uri varchar(255) NOT NULL,
    ip varchar(15) NOT NULL,
    timestamp_value TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_hits PRIMARY KEY (id)
);