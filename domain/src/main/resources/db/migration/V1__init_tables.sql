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

create table log.tags
(
  id         bigint auto_increment
        primary key,
  name       varchar(255) not null,
  `order`    int          not null,
  deleted    bit          not null,
  created_at datetime     not null,
  updated_at datetime     not null
);

create table log.tags_histories
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

create table log.posts
(
  id         bigint auto_increment
        primary key,
  user_id    bigint       not null,
  title      varchar(255) not null,
  sub_title  varchar(255) not null,
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
  sub_title  varchar(255) not null,
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

create index post_tags_post_id_index
  on log.post_tags (post_id);

create index post_tags_tag_id_index
  on log.post_tags (tag_id);

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

create table log.post_comments
(
  id          bigint auto_increment
        primary key,
  post_id     bigint       not null,
  writer_name varchar(255) not null,
  password    varchar(255) not null,
  content     text         not null,
  deleted     bit          not null,
  created_at  datetime     not null,
  updated_at  datetime     not null
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

create index channel_users_channel_id_index
  on log.channel_users (channel_id);

create index channel_users_user_id_index
  on log.channel_users (user_id);

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

create index channel_messages_channel_id_index
  on log.channel_messages (channel_id);

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

