package com.devpilot.agent.task.controller

import com.devpilot.agent.task.service.AgentTaskService
import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.task.dto.TaskCreateRequest
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.dto.TaskScheduleUpdateRequest
import com.devpilot.backend.task.dto.TaskStatusUpdateRequest
import com.devpilot.backend.task.dto.TaskTagUpdateRequest
import com.devpilot.backend.task.dto.TaskUpdateRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agent/tasks") // LLM 에이전트 전용 경로
class AgentTaskController(
    private val agentTaskService: AgentTaskService // AgentTaskService 주입
) {
    /**
     * 새로운 태스크 생성 (LLM 도구: create_task)
     * POST /api/agent/tasks/new
     */
    @PostMapping("/new")
    fun createAgentTask(
        @RequestHeader("X-User-ID") userId: Long,
        @Valid @RequestBody request: TaskCreateRequest
    ): BaseResponse<TaskResponse> {
        val response = agentTaskService.createAgentTask(userId, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 모든 태스크 조회 (LLM 도구: get_all_tasks)
     * GET /api/agent/tasks/all
     */
    @GetMapping("/all")
    fun getAllAgentTasks(@RequestHeader("X-User-ID") userId: Long): BaseResponse<List<TaskResponse>> {
        val response = agentTaskService.getAllAgentTasks(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 특정 ID의 단일 태스크 조회 (LLM 도구: get_single_task)
     * GET /api/agent/tasks/{taskId}
     */
    @GetMapping("/{taskId}")
    fun getSingleAgentTask(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable taskId: Long
    ): BaseResponse<TaskResponse> {
        val response = agentTaskService.getSingleAgentTask(userId, taskId)
        return BaseResponse.success(data = response)
    }

    /**
     * 태스크 업데이트 (LLM 도구: update_task)
     * PUT /api/agent/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    fun updateAgentTask(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: TaskUpdateRequest
    ): BaseResponse<TaskResponse> {
        val response = agentTaskService.updateAgentTask(userId, taskId, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 태스크 삭제 (LLM 도구: delete_task)
     * DELETE /api/agent/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    fun deleteAgentTask(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable taskId: Long
    ): BaseResponse<Unit> {
        val response = agentTaskService.deleteAgentTask(userId, taskId)
        return BaseResponse.success(data = response)
    }

    /**
     * 태스크 상태 업데이트 (LLM 도구: update_task_status)
     * PATCH /api/agent/tasks/{taskId}/status
     */
    @PatchMapping("/{taskId}/status")
    fun updateAgentTaskStatus(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: TaskStatusUpdateRequest
    ): BaseResponse<TaskResponse> {
        val response = agentTaskService.updateAgentTaskStatus(userId, taskId, request)
        return  BaseResponse.success(data = response)
    }

    /**
     * 태스크 태그 업데이트 (LLM 도구: update_task_tags)
     * PATCH /api/agent/tasks/{taskId}/tags
     */
    @PatchMapping("/{taskId}/tags")
    fun updateAgentTaskTags(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: TaskTagUpdateRequest
    ): BaseResponse<TaskResponse> {
        val response = agentTaskService.updateAgentTaskTags(userId, taskId, request)
         return BaseResponse.success(data = response)
    }

    /**
     * 태스크 스케줄 업데이트 (LLM 도구: update_task_schedule)
     * PATCH /api/agent/tasks/{taskId}/schedule
     */
    @PatchMapping("/{taskId}/schedule")
    fun updateAgentTaskSchedule(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: TaskScheduleUpdateRequest
    ): BaseResponse<TaskResponse> {
        val response = agentTaskService.updateAgentTaskSchedule(userId, taskId, request)
        return BaseResponse.success(data = response)
    }
}
