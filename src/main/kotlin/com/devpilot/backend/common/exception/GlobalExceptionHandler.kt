package com.devpilot.backend.common.exception

import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.exception.exceptions.DuplicateLoginIdException
import com.devpilot.backend.common.exception.exceptions.ProjectNotFoundException
import com.devpilot.backend.common.exception.exceptions.TaskNotFoundException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateLoginIdException::class)
    fun handleDuplicateLoginIdException(ex: DuplicateLoginIdException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error<Nothing>(
            code = ex.resultCode,
            message = ex.message,
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(ProjectNotFoundException::class)
    fun handleProjectNotFoundException(ex: ProjectNotFoundException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error<Nothing>(
            code = ex.resultCode,
            message = ex.message,
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFoundException(ex: TaskNotFoundException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error<Nothing>(
            code = ex.resultCode,
            message = ex.message,
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error<Nothing>(
            code = ex.resultCode,
            message = ex.message,
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }
}
