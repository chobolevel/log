create table log.notifications
(
  id         bigint       auto_increment
      primary key comment '아이디',
  user_id    bigint       not null comment '수신자 회원 아이디 (users.id FK)',
  type       varchar(50)  not null comment '알림 유형 (NotificationType enum: FOLLOW, RECORD_LIKE 등)',
  content    varchar(255) not null comment '알림 문구 (스냅샷, 생성 시점에 완성되어 저장)',
  link       varchar(255) null comment '클릭 시 이동할 경로 (스냅샷)',
  is_read    boolean      not null default false comment '읽음 여부',
  read_at    datetime     null comment '읽음 처리 일시',
  created_at datetime     not null comment '등록일시',
  constraint fk_notifications_user_id foreign key (user_id) references log.users (id) on delete cascade,
  index idx_notifications_user_id_created_at (user_id, created_at)
) comment '회원 알림 테이블';
