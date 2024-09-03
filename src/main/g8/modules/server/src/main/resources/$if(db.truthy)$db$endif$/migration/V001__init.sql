CREATE TABLE IF NOT EXISTS "users"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    email text NOT NULL UNIQUE,
    hashed_password text NOT NULL,
    age integer NOT NULL,
    pet_id integer,
    creation_date timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS "cats"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    creation_date timestamp NOT NULL
);

CREATE TABLE IF NOT EXISTS "dogs"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    age integer NOT NULL,
    creation_date timestamp NOT NULL
);

