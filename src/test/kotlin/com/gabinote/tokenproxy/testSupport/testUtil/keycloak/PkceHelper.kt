package com.gabinote.tokenproxy.testSupport.testUtil.keycloak

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * PKCE (Proof Key for Code Exchange) 관련 헬퍼 객체
 */
object PkceHelper {
    /**
     * 코드 검증자(Code Verifier)를 생성
     * @return 생성된 코드 검증자 문자열
     */
    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val codeVerifier = ByteArray(32)
        secureRandom.nextBytes(codeVerifier)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier)
    }

    /**
     * 코드 챌린지(Code Challenge)를 생성
     * @param codeVerifier 코드 검증자
     * @return 생성된 코드 챌린지 문자열
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}