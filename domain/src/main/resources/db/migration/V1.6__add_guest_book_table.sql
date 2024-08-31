create table log.guest_books
(
  id         bigint auto_increment
        primary key,
  guest_name varchar(255) not null,
  password   varchar(255) not null,
  content    text         not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create index guest_books_guest_name_index
  on log.guest_books (guest_name);

create table log.guest_books_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  guest_name varchar(255) not null,
  password   varchar(255) not null,
  content    text         not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);

