--
-- An enum type for the pet type for demonstration purposes.
-- Enumeration values are case sensitive.
--
CREATE TYPE Pet AS ENUM(
    'Cat',
    'Dog'
);

-- Needed to create a cast to be able to use the Pet type in the users table.
CREATE CAST (varchar AS Pet) WITH INOUT AS ASSIGNMENT;

CREATE TABLE IF NOT EXISTS "users"(
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    email text NOT NULL UNIQUE,
    hashed_password text NOT NULL,
    age integer NOT NULL,
    pet_type Pet,
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

