-- !Ups

CREATE TABLE Invitations (
  email VARCHAR(255),
  account_id BIGINT,
  UNIQUE(email)
);

CREATE OR REPLACE PROCEDURE upsert_user(
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
    COALESCE(
      input_account_id,
      ( SELECT account_id FROM invitations WHERE email = input_email ),
      nextval('users_account_id_seq')
    ),
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

  DELETE FROM Invitations
  WHERE email = input_email;;
$$;

-- !Downs

ALTER CREATE OR REPLACE upsert_user(
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

DROP TABLE Invitations;