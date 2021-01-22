-- !Ups

CREATE SEQUENCE users_account_id_seq START 1;

CREATE TABLE Users (
  id BIGINT GENERATED ALWAYS AS IDENTITY,
  external_source VARCHAR(10) NOT NULL,
  external_id VARCHAR(50) NOT NULL,
  account_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  preferences JSONB NOT NULL,
  PRIMARY KEY(id),
  UNIQUE(external_source, external_id)
);

CREATE INDEX users_account_id
ON Users (account_id);

CREATE TABLE Places (
  id BIGINT GENERATED ALWAYS AS IDENTITY,
  external_source VARCHAR(10) NOT NULL,
  external_id VARCHAR(50) NOT NULL,
  user_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(2000) NOT NULL,
  icon VARCHAR(2000) NOT NULL,
  visit_status VARCHAR(10) NOT NULL,
  lat DOUBLE PRECISION NOT NULL,
  lng DOUBLE PRECISION NOT NULL,
  PRIMARY KEY(id),
  UNIQUE(external_source, external_id),
  CONSTRAINT fk_places_user FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE cascade
);

CREATE TABLE PlaceComments (
  id BIGINT GENERATED ALWAYS AS IDENTITY,
  place_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_on TIMESTAMP NOT NULL,
  comment VARCHAR NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT fk_place FOREIGN KEY(place_id) REFERENCES places(id) ON DELETE cascade,
  CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE cascade
);

CREATE PROCEDURE upsert_user(
  input_external_source VARCHAR(10),
  input_external_id VARCHAR(50),
  input_account_id BIGINT,
  input_name VARCHAR(255),
  input_email VARCHAR(255),
  input_preferences JSONB
)
LANGUAGE SQL
AS $$
  INSERT INTO Users (
    external_source,
    external_id,
    account_id,
    name,
    email,
    preferences
  )
  VALUES(
    input_external_source,
    input_external_id,
    COALESCE(input_account_id, nextval('users_account_id_seq')),
    input_name,
    input_email,
    input_preferences
  )
  ON CONFLICT (external_source, external_id)
  DO
    UPDATE SET
      account_id = COALESCE(input_account_id, Users.account_id),
      name =  EXCLUDED.name,
      email = EXCLUDED.email,
      preferences = EXCLUDED.preferences;;
$$;

CREATE FUNCTION get_user_by_external_id(
  input_user_external_source VARCHAR(10),
  input_user_external_id VARCHAR(50)
)
RETURNS TABLE (
  user_id BIGINT,
  external_source VARCHAR(10),
  external_id VARCHAR(50),
  account_id BIGINT,
  name VARCHAR(255),
  email VARCHAR(255),
  preferences JSONB
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    u.id,
    u.external_source,
    u.external_id,
    u.account_id,
    u.name,
    u.email,
    u.preferences
  FROM Users u
  WHERE u.external_source = input_user_external_source
    AND u.external_id = input_user_external_id;;
END;;
$$;

CREATE FUNCTION list_users_by_place_id(
  input_place_id BIGINT
)
RETURNS TABLE (
  user_id BIGINT,
  external_source VARCHAR(10),
  external_id VARCHAR(50),
  account_id BIGINT,
  name VARCHAR(255),
  email VARCHAR(255),
  preferences JSONB
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    u.id,
    u.external_source,
    u.external_id,
    u.account_id,
    u.name,
    u.email,
    '{}'::JSONB -- don't return preferences when listing users
  FROM Users u
  JOIN Places p ON u.id = p.user_id
  WHERE p.id = input_place_id
  UNION
  SELECT DISTINCT
      u.id,
      u.external_source,
      u.external_id,
      u.account_id,
      u.name,
      u.email,
      '{}'::JSONB
    FROM PlaceComments c
    JOIN Places p ON c.place_id = p.id
    JOIN Users u ON p.user_id = u.id
    WHERE p.id = input_place_id;;
END;;
$$;

CREATE PROCEDURE upsert_place(
  input_external_source VARCHAR(10),
  input_external_id VARCHAR(50),
  input_user_id BIGINT,
  input_account_id BIGINT,
  input_name VARCHAR(255),
  input_address VARCHAR(2000),
  input_icon VARCHAR(2000),
  input_visit_status VARCHAR(10),
  input_lat DOUBLE PRECISION,
  input_lng DOUBLE PRECISION
)
LANGUAGE SQL
AS $$
    INSERT INTO Places (
      external_source,
      external_id,
      user_id,
      account_id,
      name,
      address,
      icon,
      visit_status,
      lat,
      lng
    )
    VALUES(
      input_external_source,
      input_external_id,
      input_user_id,
      input_account_id,
      input_name,
      input_address,
      input_icon,
      input_visit_status,
      input_lat,
      input_lng
    )
    ON CONFLICT (external_id, external_source)
    DO
      UPDATE SET
        user_id =      EXCLUDED.user_id,
        account_id =   EXCLUDED.account_id,
        name =         EXCLUDED.name,
        address =      EXCLUDED.address,
        icon =         EXCLUDED.icon,
        visit_status = EXCLUDED.visit_status,
        lat =          EXCLUDED.lat,
        lng =          EXCLUDED.lng;;
$$;

CREATE PROCEDURE delete_place(
  input_place_id BIGINT
)
LANGUAGE SQL
AS $$
  DELETE FROM Places
  WHERE id = input_place_id;;
$$;

CREATE FUNCTION get_place_by_id(
  input_place_id BIGINT
)
RETURNS TABLE(
  place_id BIGINT,
  external_source VARCHAR(10),
  external_id VARCHAR(50),
  user_id BIGINT,
  account_id BIGINT,
  name VARCHAR(255),
  address VARCHAR(2000),
  icon VARCHAR(2000),
  visit_status VARCHAR(10),
  lat DOUBLE PRECISION,
  lng DOUBLE PRECISION,
  comments_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    p.id,
    p.external_source,
    p.external_id,
    p.user_id,
    p.account_id,
    p.name,
    p.address,
    p.icon,
    p.visit_status,
    p.lat,
    p.lng,
    COUNT(c.id) as comments_count
  FROM Places p
  LEFT JOIN PlaceComments c ON p.id = c.place_id
  WHERE p.id = input_place_id
  GROUP BY
    p.id,
    p.external_source,
    p.external_id,
    p.user_id,
    p.account_id,
    p.name,
    p.address,
    p.icon,
    p.visit_status,
    p.lat,
    p.lng;;
END;;
$$;

CREATE FUNCTION get_place_by_external_id(
  input_external_source VARCHAR(10),
  input_external_id VARCHAR(50)
)
RETURNS TABLE(
  place_id BIGINT,
  external_source VARCHAR(10),
  external_id VARCHAR(50),
  user_id BIGINT,
  account_id BIGINT,
  name VARCHAR(255),
  address VARCHAR(2000),
  icon VARCHAR(2000),
  visit_status VARCHAR(10),
  lat DOUBLE PRECISION,
  lng DOUBLE PRECISION,
  comments_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    p.id,
    p.external_source,
    p.external_id,
    p.user_id,
    p.account_id,
    p.name,
    p.address,
    p.icon,
    p.visit_status,
    p.lat,
    p.lng,
    COUNT(c.id) as comments_count
  FROM Places p
  LEFT JOIN PlaceComments c ON p.id = c.place_id
  WHERE p.external_source = input_external_source
    AND p.external_id = input_external_id
  GROUP BY
    p.id,
    p.external_source,
    p.external_id,
    p.user_id,
    p.account_id,
    p.name,
    p.address,
    p.icon,
    p.visit_status,
    p.lat,
    p.lng;;
END;;
$$;

CREATE FUNCTION list_places_by_user_id(
  input_user_id BIGINT,
  input_visit_status VARCHAR(10)
)
RETURNS TABLE(
  place_id BIGINT,
  external_source VARCHAR(10),
  external_id VARCHAR(50),
  user_id BIGINT,
  account_id BIGINT,
  name VARCHAR(255),
  address VARCHAR(2000),
  icon VARCHAR(2000),
  visit_status VARCHAR(10),
  lat DOUBLE PRECISION,
  lng DOUBLE PRECISION,
  comments_count BIGINT
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    p.id,
    p.external_source,
    p.external_id,
    p.user_id,
    p.account_id,
    p.name,
    p.address,
    p.icon,
    p.visit_status,
    p.lat,
    p.lng,
    COUNT(c.id) as comments_count
  FROM Users u
  JOIN Places p ON u.account_id = p.account_id
  LEFT JOIN PlaceComments c ON p.id = c.place_id
  WHERE u.id = input_user_id
    AND ( input_visit_status IS NULL OR p.visit_status = input_visit_status )
  GROUP BY
    p.id,
    p.external_source,
    p.external_id,
    p.user_id,
    p.account_id,
    p.name,
    p.address,
    p.icon,
    p.visit_status,
    p.lat,
    p.lng;;
END;;
$$;

CREATE PROCEDURE insert_place_comment(
  input_place_id BIGINT,
  input_user_id BIGINT,
  input_comment VARCHAR
)
LANGUAGE SQL
AS $$
    INSERT INTO PlaceComments(
      place_id,
      user_id,
      created_on,
      comment
    )
    VALUES(
      input_place_id,
      input_user_id,
      NOW(),
      input_comment
    );;
$$;

CREATE PROCEDURE delete_place_comment(
  input_comment_id BIGINT
)
LANGUAGE SQL
AS $$
  DELETE FROM PlaceComments
  WHERE id = input_comment_id;;
$$;

CREATE FUNCTION get_place_comments_by_place_id(
  input_place_id BIGINT
)
RETURNS TABLE(
  id BIGINT,
  place_id BIGINT,
  user_id BIGINT,
  created_on TIMESTAMP,
  comment VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
  RETURN QUERY
  SELECT
    c.id,
    c.place_id,
    c.user_id,
    c.created_on,
    c.comment
  FROM PlaceComments c
  WHERE c.place_id = input_place_id;;
END;;
$$;

-- !Downs

DROP FUNCTION get_place_comments_by_place_id;
DROP PROCEDURE delete_place_comment;
DROP PROCEDURE insert_place_comment;
DROP FUNCTION list_places_by_user_id;
DROP FUNCTION get_place_by_external_id;
DROP FUNCTION get_place_by_id;
DROP PROCEDURE delete_place;
DROP PROCEDURE upsert_place;
DROP FUNCTION list_users_by_place_id;
DROP FUNCTION get_user_by_external_id;
DROP PROCEDURE upsert_user;
DROP TABLE PlaceComments;
DROP TABLE Places;
DROP INDEX users_account_id;
DROP TABLE Users;
DROP SEQUENCE users_account_id_seq;