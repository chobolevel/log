create table log.subjects_images
(
  id         bigint auto_increment comment '아이디'
        primary key,
  subject_id bigint                              not null comment '주제 아이디',
  path       varchar(255)                        not null comment '이미지 경로',
  name       varchar(255)                        not null comment '이미지 파일명',
  sort_order int                                 not null comment '이미지 정렬 순서',
  is_deleted boolean        default false        not null comment '삭제 여부',
  created_at datetime                            not null comment '등록일시',
  updated_at datetime                            not null comment '수정일시',
  constraint subjects_images_subjects_id_fk
    foreign key (subject_id) references log.subjects (id) on delete restrict
)
  comment '주제 이미지 테이블';

create index subjects_images_subject_id_index
  on log.subjects_images (subject_id);

create table log.subjects_images_histories
(
  id          bigint       not null,
  rev_id      bigint       not null,
  revtype     tinyint      not null,
  subject_id  bigint       not null,
  path        varchar(255) not null,
  name        varchar(255) not null,
  sort_order  int          not null,
  is_deleted  boolean      not null,
  created_at  datetime     not null,
  updated_at  datetime     not null,
  primary key (id, rev_id)
)
  comment '주제 이미지 이력 테이블';
