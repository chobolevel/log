-- =============================================
-- record_view_sync_events.status 인덱스
-- RecordViewSyncEventRelayScheduler.relay()가 5초마다 status='PENDING' 전체를 조회하므로
-- status 컬럼 인덱스 없이는 매 실행마다 풀 스캔이 발생한다.
-- =============================================
create index idx_record_view_sync_events_status on log.record_view_sync_events (status);
