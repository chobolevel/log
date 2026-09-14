-- =============================================
-- record_tags
-- 기록에 자유롭게 붙이는 태그 테이블
-- posts 도메인의 tags(사전 등록 필요)와 달리 미리 등록하지 않아도 임의 문자열로 입력 가능
-- 기록 수정 시 전체 교체(replace all) 방식으로 관리 — 소프트 딜리트 없이 하드 딜리트
-- =============================================
create table log.record_tags
(
    id         bigint       auto_increment primary key,
    record_id  bigint       not null,               -- 연결된 기록 (records.id FK)
    name       varchar(50)  not null,               -- 태그 이름 (자유 입력)
    created_at datetime     not null,
    updated_at datetime     not null,
    constraint fk_record_tags_record_id foreign key (record_id) references log.records (id) on delete cascade
);

-- Hibernate Envers 변경 이력 테이블
-- revtype: 0=INSERT, 1=UPDATE, 2=DELETE
create table log.record_tags_histories
(
    id         bigint      not null,
    rev_id     bigint      not null,
    revtype    tinyint     not null,
    record_id  bigint      not null,
    name       varchar(50) not null,
    created_at datetime    not null,
    updated_at datetime    not null,
    primary key (id, rev_id)
);
