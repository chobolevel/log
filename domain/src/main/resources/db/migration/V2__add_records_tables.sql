-- =============================================
-- records
-- =============================================
create table log.records
(
    id         bigint auto_increment primary key,
    user_id    bigint       not null,
    type       varchar(100) not null,
    title      varchar(255) not null,
    content    text         not null,
    is_private boolean      not null default false,
    is_deleted boolean      not null default false,
    created_at datetime     not null,
    updated_at datetime     not null
);

create index records_user_id_index
    on log.records (user_id);

create index records_type_index
    on log.records (type);

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
-- =============================================
create table log.record_reviews
(
    id              bigint       auto_increment primary key,
    record_id       bigint       not null unique,
    subject_type    varchar(100) not null,
    subject_name    varchar(255) not null,
    subject_creator varchar(255) not null,
    rating          decimal(2,1) not null,
    is_deleted      boolean      not null default false,
    created_at      datetime     not null,
    updated_at      datetime     not null
);

create table log.record_reviews_histories
(
    id              bigint       not null,
    rev_id          bigint       not null,
    revtype         tinyint      not null,
    record_id       bigint       not null,
    subject_type    varchar(100) not null,
    subject_name    varchar(255) not null,
    subject_creator varchar(255) not null,
    rating          decimal(2,1) not null,
    is_deleted      boolean      not null,
    created_at      datetime     not null,
    updated_at      datetime     not null,
    primary key (id, rev_id)
);

