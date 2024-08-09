create table log.rev_info
(
  id         bigint auto_increment
        primary key,
  revstmp    bigint                             not null,
  created_at datetime default CURRENT_TIMESTAMP not null
);

create table log.users
(
  id         bigint auto_increment
        primary key,
  email      varchar(255) not null,
  password   varchar(255) not null,
  login_type varchar(100) not null,
  nickname   varchar(100) not null,
  phone      varchar(100) not null,
  role       varchar(100) not null,
  resigned   bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create index users_email_login_type_index
  on log.users (email, login_type);

create table log.users_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  email      varchar(255) not null,
  password   varchar(255) not null,
  login_type varchar(100) not null,
  nickname   varchar(100) not null,
  phone      varchar(100) not null,
  role       varchar(100) not null,
  resigned   bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);

