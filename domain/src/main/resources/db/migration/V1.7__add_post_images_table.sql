create table log.post_images
(
  id         bigint auto_increment
        primary key,
  post_id    bigint       not null,
  type       varchar(100) not null,
  name       varchar(255) not null,
  url        varchar(255) not null,
  width      int          not null,
  height     int          not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create index post_images_post_id_index
  on log.post_images (post_id);

create table log.post_images_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  post_id    bigint       not null,
  type       varchar(100) not null,
  name       varchar(255) not null,
  url        varchar(255) not null,
  width      int          not null,
  height     int          not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);

