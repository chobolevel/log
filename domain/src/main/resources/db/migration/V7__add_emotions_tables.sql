-- =============================================
-- emotion_categories
-- 감정 카테고리 마스터 테이블 (관리자 관리)
-- type으로 긍정/부정/중립을 구분 → 주간 보고서 등 집계에 활용
-- 기쁨(POSITIVE) / 즐거움(POSITIVE) / 슬픔(NEGATIVE) / 화남(NEGATIVE)
-- =============================================
create table log.emotion_categories
(
    id         bigint      auto_increment primary key,
    name       varchar(50) not null,               -- 카테고리 이름
    type       varchar(20) not null,               -- EmotionCategoryType: POSITIVE / NEGATIVE / NEUTRAL
    `order`    int         not null,               -- 노출 순서
    is_deleted boolean     not null default false,
    created_at datetime    not null,
    updated_at datetime    not null
);

-- Hibernate Envers 변경 이력 테이블
create table log.emotion_categories_histories
(
    id         bigint      not null,
    rev_id     bigint      not null,
    revtype    tinyint     not null,
    name       varchar(50) not null,
    type       varchar(20) not null,
    `order`    int         not null,
    is_deleted boolean     not null,
    created_at datetime    not null,
    updated_at datetime    not null,
    primary key (id, rev_id)
);

-- 초기 카테고리 데이터
insert into log.emotion_categories (name, type, `order`, is_deleted, created_at, updated_at)
values ('기쁨',   'POSITIVE', 1, false, now(), now()),
       ('즐거움', 'POSITIVE', 2, false, now(), now()),
       ('슬픔',   'NEGATIVE', 3, false, now(), now()),
       ('화남',   'NEGATIVE', 4, false, now(), now());

-- =============================================
-- emotions
-- 개별 감정 마스터 테이블 (관리자 관리)
-- 카테고리 하위 소속 (예: 기쁨 → 기뻐요, 흐뭇해요, 행복해요)
-- =============================================
create table log.emotions
(
    id                  bigint      auto_increment primary key,
    emotion_category_id bigint      not null,               -- 소속 카테고리 (emotion_categories.id FK)
    name                varchar(50) not null,               -- 감정 이름
    `order`             int         not null,               -- 카테고리 내 노출 순서
    is_deleted          boolean     not null default false,
    created_at          datetime    not null,
    updated_at          datetime    not null,
    constraint fk_emotions_emotion_category_id foreign key (emotion_category_id) references log.emotion_categories (id) on delete restrict
);

-- Hibernate Envers 변경 이력 테이블
create table log.emotions_histories
(
    id                  bigint      not null,
    rev_id              bigint      not null,
    revtype             tinyint     not null,
    emotion_category_id bigint      not null,
    name                varchar(50) not null,
    `order`             int         not null,
    is_deleted          boolean     not null,
    created_at          datetime    not null,
    updated_at          datetime    not null,
    primary key (id, rev_id)
);

-- =============================================
-- record_emotions
-- DIARY 타입 기록과 감정의 연결 테이블
-- records 와 1:1 관계 (record_id unique)
-- soft delete: 주간 보고서 등 이력 집계를 위해 물리 삭제하지 않음
-- intensity: 감정의 정도 (1~10)
-- =============================================
create table log.record_emotions
(
    id         bigint   auto_increment primary key,
    record_id  bigint   not null unique,        -- 연결된 기록 (records.id FK, 1:1)
    emotion_id bigint   not null,               -- 선택된 감정 (emotions.id FK)
    intensity  int      not null,               -- 감정 강도 (1~10)
    is_deleted boolean  not null default false,
    created_at datetime not null,
    updated_at datetime not null,
    constraint fk_record_emotions_record_id  foreign key (record_id)  references log.records (id)  on delete restrict,
    constraint fk_record_emotions_emotion_id foreign key (emotion_id) references log.emotions (id) on delete restrict
);

-- Hibernate Envers 변경 이력 테이블
create table log.record_emotions_histories
(
    id         bigint   not null,
    rev_id     bigint   not null,
    revtype    tinyint  not null,
    record_id  bigint   not null,
    emotion_id bigint   not null,
    intensity  int      not null,
    is_deleted boolean  not null,
    created_at datetime not null,
    updated_at datetime not null,
    primary key (id, rev_id)
);
