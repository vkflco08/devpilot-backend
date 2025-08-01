package com.devpilot.agent.task.service

import com.devpilot.backend.task.dto.TaskCreateRequest
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.dto.TaskScheduleUpdateRequest
import com.devpilot.backend.task.dto.TaskStatusUpdateRequest
import com.devpilot.backend.task.dto.TaskTagUpdateRequest
import com.devpilot.backend.task.dto.TaskUpdateRequest
import com.devpilot.backend.task.service.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AgentTaskService(
    private val taskService: TaskService // 기존 TaskService를 주입받아 사용
) {
    /**
     * 새로운 태스크를 생성합니다. (LLM 도구: create_task)
     * LLM 에이전트의 요청을 받아 기존 TaskService의 createTask 메서드를 호출합니다.
     */
    fun createAgentTask(userId: Long, request: TaskCreateRequest): TaskResponse {
        return taskService.createTask(userId, request)
    }

    /**
     * 모든 태스크를 조회합니다. (LLM 도구: get_all_tasks)
     */
    @Transactional(readOnly = true)
    fun getAllAgentTasks(userId: Long): List<TaskResponse>? {
        return taskService.getAllTasks(userId)
    }

    /**
     * 특정 ID의 태스크를 조회합니다. (LLM 도구: get_single_task)
     */
    @Transactional(readOnly = true)
    fun getSingleAgentTask(userId: Long, taskId: Long): TaskResponse {
        return taskService.getTask(userId, taskId)
    }

    /**
     * 태스크를 업데이트합니다. (LLM 도구: update_task)
     */
    fun updateAgentTask(userId: Long, taskId: Long, request: TaskUpdateRequest): TaskResponse {
        return taskService.updateTask(userId, taskId, request)
    }

    /**
     * 태스크를 삭제합니다. (LLM 도구: delete_task)
     */
    fun deleteAgentTask(userId: Long, taskId: Long) {
        taskService.deleteTask(userId, taskId)
    }

    /**
     * 태스크 상태를 업데이트합니다. (LLM 도구: update_task_status)
     */
    fun updateAgentTaskStatus(userId: Long, taskId: Long, request: TaskStatusUpdateRequest): TaskResponse {
        return taskService.updateTaskStatus(userId, taskId, request.status)
    }

    /**
     * 태스크 태그를 업데이트합니다. (LLM 도구: update_task_tags)
     */
    fun updateAgentTaskTags(userId: Long, taskId: Long, request: TaskTagUpdateRequest): TaskResponse {
        return taskService.updateTaskTags(userId, taskId, request.tags)
    }

    /**
     * 태스크 스케줄(마감일, 우선순위)을 업데이트합니다. (LLM 도구: update_task_schedule)
     */
    fun updateAgentTaskSchedule(userId: Long, taskId: Long, request: TaskScheduleUpdateRequest): TaskResponse {
        return taskService.updateTaskSchedule(userId, taskId, request)
    }
}