package com.gabinote.tokenproxy.common.web.advice

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI
import java.util.*

/**
 * 예외 처리 어드바이스를 위한 유틸리티 클래스
 * 예외 응답 생성을 위한 공통 메서드 제공
 * @author 황준서
 */
object ExceptionAdviceHelper {
    /**
     * 요청 ID를 가져오는 메서드
     * X-Request-Id 헤더가 없으면 새로운 UUID 생성
     * @param request HTTP 요청
     * @return 요청 ID
     */
    fun getRequestId(request: HttpServletRequest): String =
        request.getHeader("X-Request-Id")?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

    /**
     * ProblemDetail 객체를 생성하는 메서드
     * RFC 7807 스펙에 맞는 문제 상세 정보 생성
     * @param status HTTP 상태 코드
     * @param title 문제 제목
     * @param detail 문제 상세 설명
     * @param type 문제 타입 URI
     * @param instance 문제 인스턴스 URI
     * @param requestId 요청 ID
     * @param additionalProperties 추가 속성 맵
     * @return 생성된 ProblemDetail 객체
     */
    fun problemDetail(
        status: HttpStatus,
        title: String? = "Unexpected Error",
        detail: String? = null,
        type: URI = URI("about:blank"),
        instance: URI? = null,
        requestId: String? = null,
        additionalProperties: Map<String, Any> = emptyMap()
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail ?: title)
        problemDetail.title = title
        problemDetail.type = type
        problemDetail.instance = instance
        problemDetail.properties = additionalProperties
        return problemDetail
    }
}