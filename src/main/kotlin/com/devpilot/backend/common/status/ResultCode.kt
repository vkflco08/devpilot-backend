package com.memo.memo.common.status

enum class ResultCode(
    val code: String,
    val msg: String,
    val status: Int,
) {
    SUCCESS("SUCCESS", "요청에 성공했습니다", 200),

    INVALID_INPUT("INVALID_INPUT", "잘못된 입력입니다", 400),
    USER_NOT_FOUND("USER_NOT_FOUND", "유저를 찾을 수 없습니다", 401),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "토큰이 만료되었습니다", 401),
    UNAUTHORIZED("UNAUTHORIZED", "인증되지 않았습니다", 401),
    SERVER_ERROR("SERVER_ERROR", "서버 에러가 발생했습니다", 500),
}
