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

CREATE TABLE if not exists roles
(
  role character varying(50) NOT NULL,
  CONSTRAINT roles_pkey PRIMARY KEY (role)
)


CREATE TABLE if not exists users
(
  userid character varying(255) NOT NULL,
  password character varying(50),
  groups text[],
  fullname character varying(255),
  avatar character varying(255),
  email character varying(255),
  created bigint,
  validated boolean,
  validationcode character varying(128),
  category integer,
  details character varying(2048),
  status integer DEFAULT 0,
  CONSTRAINT users_pkey PRIMARY KEY (userid)
)

CREATE TABLE if not exists user_roles
(
  role character varying(50) NOT NULL,
  userid character varying(50) NOT NULL,
  CONSTRAINT user_roles_pkey PRIMARY KEY (userid, role),
  CONSTRAINT user_roles_role_fkey FOREIGN KEY (role)
  REFERENCES public.roles (role) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT user_roles_userid_fkey FOREIGN KEY (userid)
  REFERENCES public.users (userid) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
)


-- CREATE TABLESPACE admin LOCATION '/var/lib/postgresql/9.6/main';
-- CREATE SEQUENCE IF NOT EXISTS system_id_seq;

-- CREATE ROLE doccloud LOGIN PASSWORD 'doccloud';
-- CREATE TABLE IF NOT EXISTS public.system
-- (
--   id integer NOT NULL DEFAULT nextval('system_id_seq'::regclass),
--   sys_date_cr timestamp without time zone NOT NULL DEFAULT now(),
--   sys_desc text,
--   sys_date_mod timestamp without time zone NOT NULL DEFAULT now(),
--   sys_title text,
--   sys_author text,
--   sys_modifier text,
--   sys_readers text[],
--   sys_editors text[],
--   sys_folders text[],
--   sys_type text,
--   sys_version text,
--   sys_parent text,
--   sys_file_path text,
--   sys_file_mime_type text,
--   sys_file_length bigint,
--   data jsonb,
--   sys_file_name text,
--   sys_uuid uuid NOT NULL DEFAULT uuid_generate_v4(),
--   sys_symbolic_name text,
--   CONSTRAINT system_pkey PRIMARY KEY (id)
-- )
-- WITH (
--   OIDS=FALSE
-- )
-- TABLESPACE admin;

-- ALTER TABLE public.system
--   OWNER TO postgres;
-- GRANT ALL ON TABLE public.system TO postgres;
-- GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE public.system TO doccloud;

-- CREATE INDEX type_index
--   ON public.system
--   USING btree
--   (sys_type COLLATE pg_catalog."default")
--   TABLESPACE admin;