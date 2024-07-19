CREATE TABLE IF NOT EXISTS "user"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    pet_id integer NOT NULL,
    creation_date timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS "cat"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    creation_date timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS "dog"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    age integer NOT NULL,
    creation_date timestamp NOT NULL
);

