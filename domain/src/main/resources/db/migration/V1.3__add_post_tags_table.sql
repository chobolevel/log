create table if not exists log.post_tags
(
  id
  bigint
  auto_increment
  primary
  key,
  name
  varchar
(
  255
) not null,
  `order` int not null,
  deleted bit not null,
  created_at datetime not null,
  updated_at datetime not null
  );

create table log.post_tags_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  name       varchar(255) not null,
  `order`    int          not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
);



