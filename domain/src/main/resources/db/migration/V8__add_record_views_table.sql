-- =============================================
-- record_views
-- 기록 조회 이력 테이블 (append-only, 유니크 제약 없음 — 재방문도 유효한 이력)
-- Redis dedup 게이트(SETNX + TTL)를 통과한 조회만 Kafka를 거쳐 이곳에 쌓인다
-- user_id/guest_id는 배타적으로 하나만 채워짐 — 회원은 user_id, 비회원은 guest_id (TSID, 13자 고정)
-- 로그인 시 guest_id 기준 이력을 user_id로 병합하며, 병합된 행은 guest_id를 null로 비움
-- 실시간 조회수는 Redis 카운터로 관리하며, 콜드스타트 시 이 테이블의 COUNT(*)로 복구
-- =============================================
create table log.record_views
(
    id         bigint      auto_increment primary key,
    record_id  bigint      not null,               -- 조회된 기록 (records.id FK)
    user_id    bigint      null,                   -- 회원 조회자 (users.id FK, guest_id와 배타적)
    guest_id   varchar(13) null,                   -- 비회원 조회자 (guest 쿠키 TSID)
    created_at datetime    not null,
    constraint fk_record_views_record_id foreign key (record_id) references log.records (id) on delete cascade,
    constraint fk_record_views_user_id foreign key (user_id) references log.users (id) on delete cascade,
    index idx_record_views_record_id (record_id),
    index idx_record_views_user_id (user_id),
    index idx_record_views_guest_id (guest_id)
);
