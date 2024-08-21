create table log.posts
(
  id         bigint auto_increment
        primary key,
  user_id    bigint       not null,
  title      varchar(255) not null,
  content    text         not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create index posts_user_id_index
  on log.posts (user_id);

create table log.posts_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  user_id    bigint       not null,
  title      varchar(255) not null,
  content    text         not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);

create table log.post_tags
(
  id         bigint auto_increment
        primary key,
  post_id    bigint   not null,
  tag_id     bigint   not null,
  created_at datetime not null,
  updated_at datetime not null
);

create table log.post_tags_histories
(
  id         bigint   not null,
  rev_id     bigint   not null,
  revtype    tinyint  not null,
  post_id    bigint   not null,
  tag_id     bigint   not null,
  created_at datetime not null,
  updated_at datetime not null,
  primary key (id, rev_id)
);

