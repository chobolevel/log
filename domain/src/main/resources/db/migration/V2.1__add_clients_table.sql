create table log.clients
(
  client_id     varchar(100) not null
    primary key,
  client_secret varchar(100) not null,
  created_at    datetime     not null,
  updated_at    datetime     not null
) comment '클라이언트 키 관리 테이블';

create table log.clients_histories
(
  client_id     varchar(100) not null,
  rev_id        bigint       not null,
  revtype       tinyint      not null,
  client_secret varchar(100) not null,
  created_at    datetime     not null,
  updated_at    datetime     not null,
  primary key (client_id, rev_id)
) comment '클라이언트 키 관리 테이블 히스토리';

