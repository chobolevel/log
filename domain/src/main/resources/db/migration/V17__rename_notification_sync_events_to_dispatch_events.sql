-- =============================================
-- notification_sync_events → notification_dispatch_events 이름 변경
-- 이 outbox 이벤트는 캐시/카운터를 "동기화"하는 좋아요/조회수/팔로우의 SyncEvent와 달리, 실시간 SSE
-- 전달을 트리거하는 역할이라 이름이 실제 역할과 맞지 않았다 (NotificationDispatchEvent로 정정).
-- 이미 적용된 V15/V16은 그대로 두고, 여기서 테이블/인덱스/제약조건명만 변경한다.
-- =============================================
rename table log.notification_sync_events to log.notification_dispatch_events;

alter table log.notification_dispatch_events
    rename index idx_notification_sync_events_status to idx_notification_dispatch_events_status;

alter table log.notification_dispatch_events
    drop foreign key fk_notification_sync_events_user_id;

alter table log.notification_dispatch_events
    rename index fk_notification_sync_events_user_id to fk_notification_dispatch_events_user_id;

alter table log.notification_dispatch_events
    add constraint fk_notification_dispatch_events_user_id foreign key (user_id) references log.users (id) on delete cascade;
