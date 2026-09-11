-- =============================================
-- record_likes
-- 기록 좋아요 테이블 (records N:M users)
-- 좋아요/취소는 row insert/delete로 처리 (소프트 딜리트 없음)
-- 실시간 집계는 Redis write-back 패턴으로 처리하며, 배치 동기화로 영속화
-- uc_record_likes_record_user 로 동일 유저의 중복 좋아요 방지
-- =============================================
create table log.record_likes
(
    id         bigint   auto_increment primary key,
    record_id  bigint   not null,               -- 좋아요 대상 기록 (records.id FK)
    user_id    bigint   not null,               -- 좋아요 누른 유저 (users.id FK)
    created_at datetime not null,
    constraint fk_record_likes_record_id foreign key (record_id) references log.records (id) on delete cascade,
    constraint fk_record_likes_user_id foreign key (user_id) references log.users (id) on delete cascade,
    constraint uc_record_likes_record_user unique (record_id, user_id)
);
