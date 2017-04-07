CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table if not exists todos (
  id serial primary key NOT NULL,
  creation_time timestamp not null default current_timestamp,
  description text,
  modification_time timestamp not null default current_timestamp,
  title text
);

create table if not exists documents (
  id serial primary key NOT NULL,
  sys_date_cr timestamp not null default current_timestamp,
  sys_desc text,
  sys_date_mod timestamp not null default current_timestamp,
  sys_title text,
  sys_author text,
  sys_modifier text,
  sys_readers text[],
  sys_editors text[],
  sys_folders text[],
  sys_type text,
  sys_version text,
  sys_parent text,
  sys_file_path text,
  sys_file_mime_type text,
  sys_file_name text,
  sys_file_length bigint,
  sys_uuid uuid NOT NULL DEFAULT uuid_generate_v4(),
  data jsonb
);

CREATE TABLE if not exists links (
	head_id		integer REFERENCES documents,
	tail_id		integer REFERENCES documents,
	PRIMARY KEY (head_id, tail_id)
);