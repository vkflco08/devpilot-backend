package com.devpilot.backend.task.controller

import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.task.dto.TaskCreateRequest
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.dto.TaskScheduleUpdateRequest
import com.devpilot.backend.task.dto.TaskStatusUpdateRequest
import com.devpilot.backend.task.dto.TaskTagUpdateRequest
import com.devpilot.backend.task.dto.TaskTimeUpdateRequest
import com.devpilot.backend.task.dto.TaskUpdateRequest
import com.devpilot.backend.task.repository.TaskRepository
import com.devpilot.backend.task.service.TaskService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/task")
class TaskController(
    private val taskService: TaskService,
    private val taskRepository: TaskRepository,
) {

    /**
     * 태스크 생성
     */
    @PostMapping("/new")
    fun create(
        @RequestBody @Valid request: TaskCreateRequest
    ): BaseResponse<TaskResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
                ?: throw UserNotFoundException()

        val response = taskService.createTask(userId, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 전체 태스크 조회
     */
    @GetMapping("/all")
    fun getAll(): BaseResponse<List<TaskResponse>> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = taskService.getAllTasks(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 단일 태스크 조회
     */
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): BaseResponse<TaskResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
                ?: throw UserNotFoundException()

        val response = taskService.getTask(userId, id)
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
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
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
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
                ?: throw UserNotFoundException()

        taskService.deleteTask(userId, id)
        return BaseResponse.success()
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: TaskStatusUpdateRequest
    ): BaseResponse<TaskResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = taskService.updateTaskStatus(userId, id, request.status)
        return BaseResponse.success(data = response)
    }

    @PatchMapping("/{id}/tags")
    fun updateTags(
        @PathVariable id: Long,
        @RequestBody request: TaskTagUpdateRequest
    ): BaseResponse<TaskResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = taskService.updateTaskTags(userId, id, request.tags)
        return BaseResponse.success(data = response)
    }

    @PatchMapping("/{id}/schedule")
    fun updateSchedule(
        @PathVariable id: Long,
        @RequestBody request: TaskScheduleUpdateRequest
    ): BaseResponse<TaskResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = taskService.updateTaskSchedule(userId, id, request)
        return BaseResponse.success(data = response)
    }

    @PatchMapping("/{id}/time")
    fun updateTime(
        @PathVariable id: Long,
        @RequestBody request: TaskTimeUpdateRequest
    ): BaseResponse<TaskResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = taskService.updateTaskTime(userId, id, request)
        return BaseResponse.success(data = response)
    }
}