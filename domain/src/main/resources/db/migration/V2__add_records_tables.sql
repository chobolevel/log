-- =============================================
-- subjects
-- 리뷰 대상(책, 영화, 드라마, 음악 등)을 통합 관리하는 테이블
-- type 컬럼으로 대상 유형을 구분하며, 외부 데이터는 배치 작업을 통해 동기화
-- 다른 테이블에서 FK로 참조되므로 하드 딜리트 없이 is_deleted 로 소프트 딜리트 처리
-- =============================================
create table log.subjects
(
    id          bigint       auto_increment primary key,
    type        varchar(100) not null,               -- 대상 유형 (SubjectType enum: BOOK, MOVIE, DRAMA, MUSIC)
    title       varchar(255) not null,               -- 대상 제목
    description text         null,                  -- 대상 간략 설명 (선택)
    is_deleted  boolean      not null default false, -- 소프트 딜리트 여부
    created_at  datetime     not null,
    updated_at  datetime     not null
);

-- Hibernate Envers 변경 이력 테이블
-- revtype: 0=INSERT, 1=UPDATE, 2=DELETE
create table log.subjects_histories
(
    id          bigint       not null,
    rev_id      bigint       not null,
    revtype     tinyint      not null,
    type        varchar(100) not null,
    title       varchar(255) not null,
    description text         null,
    is_deleted  boolean      not null,
    created_at  datetime     not null,
    updated_at  datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- records
-- 사용자가 작성하는 기록 테이블 (기술 블로그, 일상 블로그, 일기, 리뷰 등)
-- type 으로 기록 유형을 구분하며, REVIEW 타입은 record_reviews 와 1:1 관계
-- is_private=true 인 기록은 작성자 본인만 조회 가능
-- =============================================
create table log.records
(
    id         bigint       auto_increment primary key,
    user_id    bigint       not null,               -- 작성자 (users.id FK)
    type       varchar(100) not null,               -- 기록 유형 (RecordType enum: BLOG_TECH, BLOG_DAILY, DIARY, REVIEW)
    title      varchar(255) not null,               -- 기록 제목
    content    text         not null,               -- 기록 본문
    is_private boolean      not null default false, -- 비공개 여부 (true=본인만 조회 가능)
    is_deleted boolean      not null default false, -- 소프트 딜리트 여부
    created_at datetime     not null,
    updated_at datetime     not null,
    constraint fk_records_user_id foreign key (user_id) references log.users (id) on delete restrict
);

-- Hibernate Envers 변경 이력 테이블
create table log.records_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    user_id    bigint       not null,
    type       varchar(100) not null,
    title      varchar(255) not null,
    content    text         not null,
    is_private boolean      not null,
    is_deleted boolean      not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- record_reviews
-- REVIEW 타입 기록에 대한 리뷰 확장 테이블
-- records 와 1:1 관계 (record_id unique)
-- rating 은 0.5 단위, 0.5 ~ 5.0 범위
-- =============================================
create table log.record_reviews
(
    id         bigint       auto_increment primary key,
    record_id  bigint       not null unique,        -- 연결된 기록 (records.id FK, 1:1)
    subject_id bigint       not null,               -- 리뷰 대상 (subjects.id FK)
    rating     decimal(2,1) not null,               -- 평점 (0.5 단위, 0.5 ~ 5.0)
    is_deleted boolean      not null default false, -- 소프트 딜리트 여부
    created_at datetime     not null,
    updated_at datetime     not null,
    constraint fk_record_reviews_record_id foreign key (record_id) references log.records (id) on delete restrict,
    constraint fk_record_reviews_subject_id foreign key (subject_id) references log.subjects (id) on delete restrict
);

-- Hibernate Envers 변경 이력 테이블
create table log.record_reviews_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    record_id  bigint       not null,
    subject_id bigint       not null,
    rating     decimal(2,1) not null,
    is_deleted boolean      not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);
