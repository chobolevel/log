alter table posts
  add sub_title varchar(255) not null after title;

alter table posts_histories
  add sub_title varchar(255) not null after title;
