create table log.channels
(
  id         bigint auto_increment
        primary key,
  owner_id   bigint       not null,
  name       varchar(100) not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
) comment '채팅방';

create table log.channels_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  owner_id   bigint       not null,
  name       varchar(100) not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
) comment '채팅방 히스토리';

