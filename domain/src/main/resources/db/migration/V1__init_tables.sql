-- =============================================
-- revision
-- =============================================
create table log.rev_info
(
    id         bigint auto_increment primary key,
    revstmp    bigint                             not null,
    created_at datetime default CURRENT_TIMESTAMP not null
);

-- =============================================
-- users
-- =============================================
create table log.users
(
    id         bigint auto_increment primary key,
    email      varchar(255) not null,
    password   varchar(255) not null,
    social_id  varchar(255) null,
    login_type varchar(100) not null,
    nickname   varchar(100) not null,
    role       varchar(100) not null,
    resigned   bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
);

create index users_email_login_type_index
    on log.users (email, login_type);

create table log.users_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    email      varchar(255) not null,
    password   varchar(255) not null,
    social_id  varchar(255) null,
    login_type varchar(100) not null,
    nickname   varchar(100) not null,
    role       varchar(100) not null,
    resigned   bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- users_images
-- =============================================
create table log.users_images
(
    id         bigint auto_increment primary key,
    user_id    bigint       not null,
    type       varchar(100) not null,
    path       varchar(255) not null,
    name       varchar(255) not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
);

create index users_images_user_id_index
    on log.users_images (user_id);

create table log.users_images_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    user_id    bigint       not null,
    type       varchar(100) not null,
    path       varchar(255) not null,
    name       varchar(255) not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- tags
-- =============================================
create table log.tags
(
    id         bigint auto_increment primary key,
    name       varchar(255) not null,
    `order`    int          not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
);

create table log.tags_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    name       varchar(255) not null,
    `order`    int          not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- posts
-- =============================================
create table log.posts
(
    id         bigint auto_increment primary key,
    user_id    bigint       not null,
    title      varchar(255) not null,
    sub_title  varchar(255) not null,
    content    text         not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
);

create index posts_user_id_index
    on log.posts (user_id);

create table log.posts_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    user_id    bigint       not null,
    title      varchar(255) not null,
    sub_title  varchar(255) not null,
    content    text         not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- post_tags
-- =============================================
create table log.post_tags
(
    id         bigint auto_increment primary key,
    post_id    bigint   not null,
    tag_id     bigint   not null,
    created_at datetime not null,
    updated_at datetime not null
);

create index post_tags_post_id_index
    on log.post_tags (post_id);

create index post_tags_tag_id_index
    on log.post_tags (tag_id);

create table log.post_tags_histories
(
    id         bigint   not null,
    rev_id     bigint   not null,
    revtype    tinyint  not null,
    post_id    bigint   not null,
    tag_id     bigint   not null,
    created_at datetime not null,
    updated_at datetime not null,
    primary key (id, rev_id)
);

-- =============================================
-- post_images
-- =============================================
create table log.post_images
(
    id         bigint auto_increment primary key,
    post_id    bigint       not null,
    type       varchar(100) not null,
    name       varchar(255) not null,
    path       varchar(255) not null,
    width      int          not null,
    height     int          not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
);

create index post_images_post_id_index
    on log.post_images (post_id);

create table log.post_images_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    post_id    bigint       not null,
    type       varchar(100) not null,
    name       varchar(255) not null,
    path       varchar(255) not null,
    width      int          not null,
    height     int          not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- post_comments
-- =============================================
create table log.post_comments
(
    id         bigint auto_increment primary key,
    post_id    bigint not null,
    writer_id  bigint not null,
    content    text   not null,
    deleted    bit    not null,
    created_at datetime not null,
    updated_at datetime not null
);

create index post_comments_post_id_index
    on log.post_comments (post_id);

create table log.post_comments_histories
(
    id         bigint   not null,
    rev_id     bigint   not null,
    revtype    tinyint  not null,
    post_id    bigint   not null,
    writer_id  bigint   not null,
    content    text     not null,
    deleted    bit      not null,
    created_at datetime not null,
    updated_at datetime not null,
    primary key (id, rev_id)
);

-- =============================================
-- guest_books
-- =============================================
create table log.guest_books
(
    id         bigint auto_increment primary key,
    guest_name varchar(255) not null,
    password   varchar(255) not null,
    content    text         not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
);

create index guest_books_guest_name_index
    on log.guest_books (guest_name);

create table log.guest_books_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    guest_name varchar(255) not null,
    password   varchar(255) not null,
    content    text         not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
);

-- =============================================
-- channels
-- =============================================
create table log.channels
(
    id         bigint auto_increment primary key,
    owner_id   bigint       not null,
    name       varchar(100) not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
) comment '채팅방';

create table log.channels_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    owner_id   bigint       not null,
    name       varchar(100) not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
) comment '채팅방 히스토리';

-- =============================================
-- channel_users
-- =============================================
create table log.channel_users
(
    id         bigint auto_increment primary key,
    channel_id bigint   not null,
    user_id    bigint   not null,
    deleted    bit      not null,
    created_at datetime not null,
    updated_at datetime not null
) comment '대화 채널 참여 회원';

create index channel_users_channel_id_index
    on log.channel_users (channel_id);

create index channel_users_user_id_index
    on log.channel_users (user_id);

create table log.channel_users_histories
(
    id         bigint   not null,
    rev_id     bigint   not null,
    revtype    tinyint  not null,
    channel_id bigint   not null,
    user_id    bigint   not null,
    deleted    bit      not null,
    created_at datetime not null,
    updated_at datetime not null,
    primary key (id, rev_id)
) comment '대화 채널 참여 회원 히스토리';

-- =============================================
-- channel_messages
-- =============================================
create table log.channel_messages
(
    id         bigint auto_increment primary key,
    channel_id bigint       not null,
    writer_id  bigint       not null,
    type       varchar(100) not null,
    content    varchar(255) not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null
) comment '대화 채널 메세지';

create index channel_messages_channel_id_index
    on log.channel_messages (channel_id);

create table log.channel_messages_histories
(
    id         bigint       not null,
    rev_id     bigint       not null,
    revtype    tinyint      not null,
    channel_id bigint       not null,
    writer_id  bigint       not null,
    type       varchar(100) not null,
    content    varchar(255) not null,
    deleted    bit          not null,
    created_at datetime     not null,
    updated_at datetime     not null,
    primary key (id, rev_id)
) comment '대화 채널 메세지 히스토리';

-- =============================================
-- batch
-- =============================================
create table log.BATCH_STEP_EXECUTION_SEQ
(
    ID bigint not null
);
insert into log.BATCH_STEP_EXECUTION_SEQ values (0);

create table log.BATCH_JOB_EXECUTION_SEQ
(
    ID bigint not null
);
insert into log.BATCH_JOB_EXECUTION_SEQ values (0);

create table log.BATCH_JOB_SEQ
(
    ID bigint not null
);
insert into log.BATCH_JOB_SEQ values (0);

create table log.BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID bigint       primary key,
    VERSION         bigint,
    JOB_NAME        varchar(100) not null,
    JOB_KEY         varchar(32)  not null
);

create table log.BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID bigint    primary key,
    VERSION          bigint,
    JOB_INSTANCE_ID  bigint    not null,
    CREATE_TIME      timestamp not null,
    START_TIME       timestamp default null,
    END_TIME         timestamp default null,
    STATUS           varchar(10),
    EXIT_CODE        varchar(20),
    EXIT_MESSAGE     varchar(2500),
    LAST_UPDATED     timestamp,
    constraint JOB_INSTANCE_EXECUTION_FK foreign key (JOB_INSTANCE_ID) references log.BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
);

create table log.BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID bigint        not null,
    PARAMETER_NAME   varchar(100)  not null,
    PARAMETER_TYPE   varchar(100)  not null,
    PARAMETER_VALUE  varchar(2500),
    IDENTIFYING      char(1)       not null,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID) references log.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table log.BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  bigint        not null primary key,
    VERSION            bigint        not null,
    STEP_NAME          varchar(100)  not null,
    JOB_EXECUTION_ID   bigint        not null,
    CREATE_TIME        timestamp     not null,
    START_TIME         timestamp default null,
    END_TIME           timestamp default null,
    STATUS             varchar(10),
    COMMIT_COUNT       bigint,
    READ_COUNT         bigint,
    FILTER_COUNT       bigint,
    WRITE_COUNT        bigint,
    READ_SKIP_COUNT    bigint,
    PROCESS_SKIP_COUNT bigint,
    ROLLBACK_COUNT     bigint,
    EXIT_CODE          varchar(20),
    EXIT_MESSAGE       varchar(2500),
    LAST_UPDATED       timestamp,
    constraint JOB_EXECUTION_STEP_FK foreign key (JOB_EXECUTION_ID) references log.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table log.BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   bigint        primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID) references log.BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
);

create table log.BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  bigint        primary key,
    SHORT_CONTEXT      varchar(2500) not null,
    SERIALIZED_CONTEXT text,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID) references log.BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
);
