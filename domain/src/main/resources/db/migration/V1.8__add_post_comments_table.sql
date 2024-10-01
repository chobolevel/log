create table if not exists log.post_comments
(
  id
  bigint
  auto_increment
  primary
  key,
  post_id
  bigint
  not
  null,
  writer_name
  varchar
(
  255
) not null,
  password varchar
(
  255
) not null,
  content text not null,
  deleted bit not null,
  created_at datetime not null,
  updated_at datetime not null
  );

create index post_comments_post_id_index
  on log.post_comments (post_id);

create table log.post_comments_histories
(
  id          bigint       not null,
  rev_id      bigint       not null,
  revtype     tinyint      not null,
  post_id     bigint       not null,
  writer_name varchar(255) not null,
  password    varchar(255) not null,
  content     text         not null,
  deleted     bit          not null,
  created_at  datetime     not null,
  updated_at  datetime     not null,
  primary key (id, rev_id)
);
