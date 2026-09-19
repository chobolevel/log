-- =============================================
-- user_follow_sync_events (outbox)
-- 팔로우/언팔로우 이벤트를 Kafka에 발행하기 전 임시 보관하는 outbox 테이블
-- follow()/unfollow() 와 같은 트랜잭션에서 INSERT → Relay 스케줄러가 Kafka 발행 후 PUBLISHED → Consumer가 DB 동기화 후 PROCESSED
-- action: FOLLOW / UNFOLLOW
-- status: PENDING / PUBLISHED / PROCESSED / FAILED
-- =============================================
create table log.user_follow_sync_events
(
  id                 bigint      auto_increment
      primary key comment '아이디',
  follower_user_id   bigint      not null comment '팔로워 회원 아이디',
  following_user_id  bigint      not null comment '팔로잉 회원 아이디',
  action             varchar(10) not null comment '이벤트 액션 (FOLLOW/UNFOLLOW)',
  status             varchar(10) not null default 'PENDING' comment '처리 상태 (PENDING/PUBLISHED/PROCESSED/FAILED)',
  created_at         datetime    not null comment '등록일시',
  published_at       datetime    null comment 'Kafka 발행일시',
  retry_count        int         not null default 0 comment '재시도 횟수',
  constraint fk_user_follow_sync_events_follower_user_id foreign key (follower_user_id) references log.users (id) on delete cascade,
  constraint fk_user_follow_sync_events_following_user_id foreign key (following_user_id) references log.users (id) on delete cascade
) comment '팔로우/언팔로우 이벤트 아웃박스';
