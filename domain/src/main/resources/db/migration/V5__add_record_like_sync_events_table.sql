-- =============================================
-- record_like_sync_events (outbox)
-- 좋아요/취소 이벤트를 Kafka에 발행하기 전 임시 보관하는 outbox 테이블
-- like()/dislike() 와 같은 트랜잭션에서 INSERT → Relay 스케줄러가 Kafka 발행 후 PUBLISHED → Consumer가 DB 동기화 후 PROCESSED
-- action: LIKE / DISLIKE
-- status: PENDING / PUBLISHED / PROCESSED / FAILED
-- =============================================
create table log.record_like_sync_events
(
    id           bigint      auto_increment primary key,
    record_id    bigint      not null,
    user_id      bigint      not null,
    action       varchar(10) not null,
    status       varchar(10) not null default 'PENDING',
    created_at   datetime    not null,
    published_at datetime,
    retry_count  int         not null default 0,
    constraint fk_record_like_sync_events_record_id foreign key (record_id) references log.records (id) on delete cascade,
    constraint fk_record_like_sync_events_user_id foreign key (user_id) references log.users (id) on delete cascade
);
