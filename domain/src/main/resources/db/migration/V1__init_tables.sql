DROP TABLE IF EXISTS log.rev_info;

create table log.rev_info
(
  id         bigint auto_increment
        primary key,
  revstmp    bigint                             not null,
  created_at datetime default CURRENT_TIMESTAMP not null
);

DROP TABLE IF EXISTS log.users;

create table log.users
(
  id         bigint auto_increment
        primary key,
  email      varchar(255) not null,
  password   varchar(255) not null,
  social_id varchar(255) null,
  login_type varchar(100) not null,
  nickname   varchar(100) not null,
  role       varchar(100) not null,
  resigned   bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create index users_email_login_type_index
  on log.users (email, login_type);

create index users_social_id_login_type_index
  on log.users (social_id, login_type);

DROP TABLE IF EXISTS log.users_histories;

create table log.users_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  email      varchar(255) not null,
  password   varchar(255) not null,
  social_id varchar(255) null,
  login_type varchar(100) not null,
  nickname   varchar(100) not null,
  role       varchar(100) not null,
  resigned   bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);



