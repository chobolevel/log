create table log.channel_users
(
  id         bigint auto_increment
        primary key,
  channel_id bigint   not null,
  user_id    bigint   not null,
  deleted    bit      not null,
  created_at datetime not null,
  updated_at datetime not null
) comment '대화 채널 참여 회원';

create table log.channel_users_histories
(
  id         bigint   not null,
  rev_id     bigint   not null,
  revtype    tinyint  not null,
  channel_id bigint   not null,
  user_id    bigint   not null,
  deleted    bit      not null,
  created_at datetime not null,
  updated_at datetime not null,
  primary key (id, rev_id)
) comment '대화 채널 참여 회원 히스토리';

create table log.channel_messages
(
  id         bigint auto_increment
        primary key,
  channel_id bigint       not null,
  writer_id  bigint       not null,
  type       varchar(100) not null,
  content    varchar(255) not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
) comment '대화 채널 메세지';

create table log.channel_messages_histories
(
  id         bigint       not null,
  rev_id     bigint       not null,
  revtype    tinyint      not null,
  channel_id bigint       not null,
  writer_id  bigint       not null,
  type       varchar(100) not null,
  content    varchar(255) not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null,
  primary key (id, rev_id)
) comment '대화 채널 메세지 히스토리';

