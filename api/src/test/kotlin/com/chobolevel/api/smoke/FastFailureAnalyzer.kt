package com.chobolevel.api.smoke

import org.springframework.beans.BeansException
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis
import org.springframework.core.Ordered

// 빈 누락 같은 BeansException으로 컨텍스트가 실패하면, Boot의 NoSuchBeanDefinitionFailureAnalyzer는 "왜 없는지"를 설명하려고
// 모든 빈 정의의 타입을 다시 예측한다. 이 앱처럼 빈이 많으면 분석에만 수 분 이상 걸려 테스트가 멈춘 것처럼 보인다
// (모듈 이동으로 배선이 깨졌을 때 스모크 테스트가 빠르게 실패해야 하는데 CI가 매달린다).
// 그 분석기보다 먼저 실행되어 원인 메시지만 담은 결과를 즉시 반환한다.
class FastFailureAnalyzer : AbstractFailureAnalyzer<BeansException>(), Ordered {

    override fun analyze(rootFailure: Throwable, cause: BeansException): FailureAnalysis {
        return FailureAnalysis(
            "애플리케이션 컨텍스트 로딩 실패: ${cause.mostSpecificCause.message}",
            "예외 체인의 가장 안쪽 원인을 확인하세요. (상세 분석은 테스트에서 생략됨)",
            rootFailure
        )
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
