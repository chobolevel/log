-- =============================================
-- record_view_sync_events (outbox)
-- 조회 이벤트를 Kafka에 발행하기 전 임시 보관하는 outbox 테이블
-- recordView()와 같은 트랜잭션에서 INSERT → Relay 스케줄러가 Kafka 발행 후 PUBLISHED → Consumer가 캐시 동기화 후 PROCESSED
-- status: PENDING / PUBLISHED / PROCESSED / FAILED
-- =============================================
create table log.record_view_sync_events
(
  id           bigint   auto_increment
      primary key comment '아이디',
  record_id    bigint   not null comment '대상 기록 (records.id FK)',
  status       varchar(10) not null default 'PENDING' comment '처리 상태 (PENDING/PUBLISHED/PROCESSED/FAILED)',
  created_at   datetime not null comment '등록일시',
  published_at datetime null comment 'Kafka 발행일시',
  retry_count  int      not null default 0 comment '재시도 횟수',
  constraint fk_record_view_sync_events_record_id foreign key (record_id) references log.records (id) on delete cascade
) comment '조회 이벤트 아웃박스';
