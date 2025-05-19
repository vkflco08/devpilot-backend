package com.devpilot.backend.common.dto

data class BaseResponse<T>(
    val resultCode: String,
    val message: String,
    val data: T? = null,
    val httpStatus: Int,
) {
    companion object {
        fun <T> success(
            data: T? = null,
            message: String = "요청에 성공했습니다",
        ): BaseResponse<T> =
            BaseResponse(
                resultCode = "SUCCESS",
                message = message,
                data = data,
                httpStatus = 200,
            )

        fun <T> error(
            code: String,
            message: String,
            status: Int,
            data: T? = null,
        ): BaseResponse<T> =
            BaseResponse(
                resultCode = code,
                message = message,
                data = data,
                httpStatus = status,
            )
    }
}
