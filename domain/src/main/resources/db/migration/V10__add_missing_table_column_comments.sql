-- =============================================
-- V4~V8에서 누락된 테이블/컬럼 주석 보완
-- 신규 테이블은 항상 테이블 코멘트 + 컬럼 코멘트를 다는 것을 원칙으로 하며,
-- 이 마이그레이션은 그 원칙이 적용되기 전에 만들어진 테이블에 대한 소급 보완이다.
-- 히스토리 테이블은 기존 컨벤션(V1 channels_histories 등)에 맞춰 테이블 코멘트만 부여한다.
-- =============================================

-- record_likes
alter table log.record_likes comment = '기록 좋아요';
alter table log.record_likes modify column id bigint not null auto_increment comment '아이디';
alter table log.record_likes modify column record_id bigint not null comment '좋아요 대상 기록 (records.id FK)';
alter table log.record_likes modify column user_id bigint not null comment '좋아요 누른 회원 (users.id FK)';
alter table log.record_likes modify column created_at datetime not null comment '등록일시';

-- record_like_sync_events
alter table log.record_like_sync_events comment = '좋아요/취소 이벤트 아웃박스';
alter table log.record_like_sync_events modify column id bigint not null auto_increment comment '아이디';
alter table log.record_like_sync_events modify column record_id bigint not null comment '대상 기록 (records.id FK)';
alter table log.record_like_sync_events modify column user_id bigint not null comment '이벤트 발생 회원 (users.id FK)';
alter table log.record_like_sync_events modify column action varchar(10) not null comment '이벤트 액션 (LIKE/DISLIKE)';
alter table log.record_like_sync_events modify column status varchar(10) not null default 'PENDING' comment '처리 상태 (PENDING/PUBLISHED/PROCESSED/FAILED)';
alter table log.record_like_sync_events modify column created_at datetime not null comment '등록일시';
alter table log.record_like_sync_events modify column published_at datetime null comment 'Kafka 발행일시';
alter table log.record_like_sync_events modify column retry_count int not null default 0 comment '재시도 횟수';

-- record_tags
alter table log.record_tags comment = '기록 태그';
alter table log.record_tags modify column id bigint not null auto_increment comment '아이디';
alter table log.record_tags modify column record_id bigint not null comment '연결된 기록 (records.id FK)';
alter table log.record_tags modify column name varchar(50) not null comment '태그 이름 (자유 입력)';
alter table log.record_tags modify column created_at datetime not null comment '등록일시';
alter table log.record_tags modify column updated_at datetime not null comment '수정일시';

alter table log.record_tags_histories comment = '기록 태그 히스토리';

-- emotion_categories
alter table log.emotion_categories comment = '감정 카테고리 마스터';
alter table log.emotion_categories modify column id bigint not null auto_increment comment '아이디';
alter table log.emotion_categories modify column name varchar(50) not null comment '카테고리 이름';
alter table log.emotion_categories modify column type varchar(20) not null comment '카테고리 타입 (POSITIVE/NEGATIVE/NEUTRAL)';
alter table log.emotion_categories modify column `order` int not null comment '노출 순서';
alter table log.emotion_categories modify column is_deleted boolean not null default false comment '삭제 여부';
alter table log.emotion_categories modify column created_at datetime not null comment '등록일시';
alter table log.emotion_categories modify column updated_at datetime not null comment '수정일시';

alter table log.emotion_categories_histories comment = '감정 카테고리 히스토리';

-- emotions
alter table log.emotions comment = '감정 마스터';
alter table log.emotions modify column id bigint not null auto_increment comment '아이디';
alter table log.emotions modify column emotion_category_id bigint not null comment '소속 카테고리 (emotion_categories.id FK)';
alter table log.emotions modify column name varchar(50) not null comment '감정 이름';
alter table log.emotions modify column `order` int not null comment '카테고리 내 노출 순서';
alter table log.emotions modify column is_deleted boolean not null default false comment '삭제 여부';
alter table log.emotions modify column created_at datetime not null comment '등록일시';
alter table log.emotions modify column updated_at datetime not null comment '수정일시';

alter table log.emotions_histories comment = '감정 히스토리';

-- record_emotions
alter table log.record_emotions comment = '기록 감정 연결';
alter table log.record_emotions modify column id bigint not null auto_increment comment '아이디';
alter table log.record_emotions modify column record_id bigint not null comment '연결된 기록 (records.id FK, 1:1)';
alter table log.record_emotions modify column emotion_id bigint not null comment '선택된 감정 (emotions.id FK)';
alter table log.record_emotions modify column intensity int not null comment '감정 강도 (1~10)';
alter table log.record_emotions modify column is_deleted boolean not null default false comment '삭제 여부';
alter table log.record_emotions modify column created_at datetime not null comment '등록일시';
alter table log.record_emotions modify column updated_at datetime not null comment '수정일시';

alter table log.record_emotions_histories comment = '기록 감정 연결 히스토리';

-- record_views
alter table log.record_views comment = '기록 조회 이력';
alter table log.record_views modify column id bigint not null auto_increment comment '아이디';
alter table log.record_views modify column record_id bigint not null comment '조회된 기록 (records.id FK)';
alter table log.record_views modify column user_id bigint null comment '회원 조회자 (users.id FK, guest_id와 배타적)';
alter table log.record_views modify column guest_id varchar(13) null comment '비회원 조회자 (guest 쿠키 TSID)';
alter table log.record_views modify column created_at datetime not null comment '등록일시';
