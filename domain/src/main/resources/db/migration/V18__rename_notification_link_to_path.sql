-- =============================================
-- notifications / notification_dispatch_events 의 link 컬럼을 path로 이름 변경
-- 저장하는 값이 호스트 없는 상대경로(예: /users/1)이므로 link(절대 URL을 연상시킴)보다 path가 실제 의미에 맞다.
-- 외부 채널(이메일/푸시)이 추가되면 발송 시점에 host + path로 조합하며, 저장 값은 그대로 둔다.
-- =============================================
alter table log.notifications
    change column link path varchar(255) null comment '클릭 시 이동할 상대경로 (스냅샷, 호스트 제외)';

alter table log.notification_dispatch_events
    change column link path varchar(255) null comment '클릭 시 이동할 상대경로 (스냅샷, 호스트 제외)';
