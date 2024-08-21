create table log.users_images
(
  id         bigint auto_increment
        primary key,
  user_id    bigint       not null,
  type       varchar(100) not null,
  origin_url varchar(255) not null,
  name       varchar(255) not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create index users_images_user_id_index
  on log.users_images (user_id);

create table log.users_images_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  user_id    bigint       not null,
  type       varchar(100) not null,
  origin_url varchar(255) not null,
  name       varchar(255) not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);

