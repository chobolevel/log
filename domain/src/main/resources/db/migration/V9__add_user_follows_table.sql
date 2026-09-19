create table log.user_follows
(
  id                 bigint    auto_increment
      primary key comment '아이디',
  follower_user_id   bigint    not null comment '팔로워 회원 아이디',
  following_user_id  bigint    not null comment '팔로잉 회원 아이디',
  created_at         datetime  not null comment '등록일시',
  constraint fk_user_follows_follower_user_id foreign key (follower_user_id) references log.users (id) on delete cascade,
  constraint fk_user_follows_following_user_id foreign key (following_user_id) references log.users (id) on delete cascade,
  constraint uc_user_follows_follower_following unique (follower_user_id, following_user_id),
  index idx_user_follows_follower_user_id (follower_user_id),
  index idx_user_follows_following_user_id (following_user_id)
) comment '회원 팔로우 테이블';
