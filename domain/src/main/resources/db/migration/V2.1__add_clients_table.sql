create table log.clients
(
  id           varchar(100) not null
    primary key,
  user_id      bigint       not null,
  secret       varchar(100) not null,
  name         varchar(100) not null,
  redirect_url varchar(255) not null,
  deleted      bit          not null,
  created_at   datetime     not null,
  updated_at   datetime     not null
) comment '클라이언트 키 관리 테이블';

create table log.clients_histories
(
  id           varchar(100) not null,
  rev_id       bigint       not null,
  revtype      tinyint      not null,
  user_id      bigint       not null,
  secret       varchar(100) not null,
  name         varchar(100) not null,
  redirect_url varchar(255) not null,
  deleted      bit          not null,
  created_at   datetime     not null,
  updated_at   datetime     not null,
  primary key (id, rev_id)
) comment '클라이언트 키 관리 테이블 히스토리';

