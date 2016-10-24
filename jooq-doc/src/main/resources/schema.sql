create table if not exists todos (
  id serial primary key NOT NULL,
  creation_time timestamp not null default current_timestamp,
  description text,
  modification_time timestamp not null default current_timestamp,
  title text
);