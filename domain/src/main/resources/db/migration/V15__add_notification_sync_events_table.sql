-- =============================================
-- notification_sync_events (outbox)
-- 알림 발송 이벤트를 Kafka에 발행하기 전 임시 보관하는 outbox 테이블
-- NotificationProvider.push() 호출 시 같은 트랜잭션에서 INSERT
-- → Relay 스케줄러가 Kafka 발행 후 PUBLISHED → Consumer가 notifications row 생성 후 PROCESSED
-- status: PENDING / PUBLISHED / PROCESSED / FAILED
-- =============================================
create table log.notification_sync_events
(
  id           bigint       auto_increment
      primary key comment '아이디',
  user_id      bigint       not null comment '수신자 회원 아이디 (users.id FK)',
  type         varchar(50)  not null comment '알림 유형 (NotificationType enum)',
  content      varchar(255) not null comment '알림 문구 (스냅샷)',
  link         varchar(255) null comment '클릭 시 이동할 경로 (스냅샷)',
  status       varchar(10)  not null default 'PENDING' comment '처리 상태 (PENDING/PUBLISHED/PROCESSED/FAILED)',
  created_at   datetime     not null comment '등록일시',
  published_at datetime     null comment 'Kafka 발행일시',
  retry_count  int          not null default 0 comment '재시도 횟수',
  constraint fk_notification_sync_events_user_id foreign key (user_id) references log.users (id) on delete cascade
) comment '알림 발송 이벤트 아웃박스';
