alter table log.users
  add social_id varchar(255) null after password;

create index users_social_id_login_type_index
  on log.users (social_id, login_type);

alter table log.users_histories
  add social_id varchar(255) null after password
