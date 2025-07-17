package com.devpilot.backend.task.service

import com.devpilot.backend.common.exception.exceptions.ProjectNotFoundException
import com.devpilot.backend.common.exception.exceptions.TaskNotFoundException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.member.repository.MemberRepository
import com.devpilot.backend.project.entity.Project
import com.devpilot.backend.project.repository.ProjectRepository
import com.devpilot.backend.task.dto.TaskCreateRequest
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.dto.TaskScheduleUpdateRequest
import com.devpilot.backend.task.dto.TaskTimeUpdateRequest
import com.devpilot.backend.task.dto.TaskUpdateRequest
import com.devpilot.backend.task.entity.Task
import com.devpilot.backend.task.entity.TaskStatus
import com.devpilot.backend.task.repository.TaskRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val memberRepository : MemberRepository,
    private val projectRepository: ProjectRepository,
) {

    fun getTask(userId: Long, id: Long): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(id, userId)
            ?: throw TaskNotFoundException()
        return task.toResponse()
    }

    @Transactional
    fun createTask(userId: Long, request: TaskCreateRequest): TaskResponse {
        val member = memberRepository.findById(userId).orElseThrow { UserNotFoundException() } // 이 부분에서 userId가 null이면 UserNotFoundException 발생

        val project = request.projectId?.let { projectId ->
            projectRepository.findByIdAndMemberId(projectId, userId)
                ?: throw ProjectNotFoundException()
        }

        val parentTask = request.parentId?.let { parentId ->
            taskRepository.findByIdAndMemberId(parentId, userId)
                ?: throw TaskNotFoundException()
        }

        val task = Task(
            title = request.title,
            description = request.description,
            status = request.status,
            tags = request.tags,
            priority = request.priority ?: 3,
            dueDate = request.dueDate,
            estimatedTimeHours = request.estimatedTimeHours,
            member = member,
            project = project,
            parent = parentTask,
            previousStatus = request.previousStatus
        )

        val savedTask = taskRepository.save(task)
        return savedTask.toResponse()
    }

    @Transactional
    fun updateTask(userId: Long, taskId: Long, request: TaskUpdateRequest): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(taskId, userId)
            ?: throw TaskNotFoundException()

        request.title?.let { task.title = it }
        request.description?.let { task.description = it }
        request.status?.let { task.status = it }
        request.tags?.let { task.tags = it }
        request.priority?.let { task.priority = it }
        request.dueDate?.let { task.dueDate = it }
        request.estimatedTimeHours?.let { task.estimatedTimeHours = it }
        request.previousStatus?.let { task.previousStatus = it }

        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    @Transactional
    fun deleteTask(userId: Long, id: Long) {
        val task = taskRepository.findByIdAndMemberId(id, userId)
            ?: throw TaskNotFoundException()
        taskRepository.delete(task)
    }

    @Transactional
    fun updateTaskStatus(userId: Long, taskId: Long, status: TaskStatus): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(taskId, userId)
            ?: throw TaskNotFoundException()

        if (status == TaskStatus.DONE) {
            task.previousStatus = task.status // 현재 상태를 이전 상태로 저장
        } else if (task.status == TaskStatus.DONE && status != TaskStatus.DONE) {
            // DONE에서 다른 상태로 변경될 경우 previousStatus를 초기화 (선택적)
            task.previousStatus = null
        }
        task.status = status

        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    @Transactional
    fun updateTaskTags(userId: Long, taskId: Long, tags: List<String>): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(taskId, userId)
            ?: throw TaskNotFoundException()
        task.tags = tags.joinToString(",")
        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    @Transactional
    fun updateTaskSchedule(userId: Long, taskId: Long, request: TaskScheduleUpdateRequest): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(taskId, userId)
            ?: throw TaskNotFoundException()
        task.priority = request.priority
        task.dueDate = request.dueDate
        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    @Transactional
    fun updateTaskTime(userId: Long, taskId: Long, request: TaskTimeUpdateRequest): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(taskId, userId)
            ?: throw TaskNotFoundException()
        // Convert minutes to hours
        task.estimatedTimeHours = request.estimatedTimeHours?.div(60.0)

        val updated = taskRepository.save(task)
        return updated.toResponse()
    }

    fun getAllTasks(userId: Long): List<TaskResponse>? {
        val findTasks: List<Task> = taskRepository.findAllByMemberIdAndProjectStatus(userId)
        return findTasks.map { task -> task.toResponse() }
    }
}
