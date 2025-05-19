package com.devpilot.backend.task.controller

import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.CustomUser
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.task.dto.TaskCreateRequest
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.dto.TaskUpdateRequest
import com.devpilot.backend.task.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService,
) {

    /**
     * 태스크 생성
     */
    @PostMapping
    fun create(
        @RequestBody @Valid request: TaskCreateRequest
    ): BaseResponse<TaskResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val response = taskService.createTask(userId, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 단일 태스크 조회
     */
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): BaseResponse<TaskResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val response = taskService.getTask(userId, id)
        return BaseResponse.success(data = response)
    }

    /**
     * 전체 태스크 조회
     */
    @GetMapping
    fun getAll(): BaseResponse<List<TaskResponse>> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val response = taskService.getAllTasks(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 태스크 수정
     */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid request: TaskUpdateRequest
    ): BaseResponse<TaskResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val response = taskService.updateTask(userId, id, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 태스크 삭제
     */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseResponse<Unit> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        taskService.deleteTask(userId, id)
        return BaseResponse.success()
    }
}