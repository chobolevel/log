package com.chobolevel.api.record.feed.performance

import org.hibernate.resource.jdbc.spi.StatementInspector
import java.util.concurrent.CopyOnWriteArrayList

// Hibernate가 실제로 내보낸 SQL을 기록한다. 성능 테스트가 실행 계획과 쿼리 수를 같은 SQL로 확인하는 데 쓴다.
class RecordingStatementInspector : StatementInspector {

    companion object {
        val statements: MutableList<String> = CopyOnWriteArrayList()
    }

    override fun inspect(sql: String): String {
        statements.add(sql)
        return sql
    }
}
