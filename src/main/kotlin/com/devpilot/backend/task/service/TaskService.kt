package com.devpilot.backend.task.service

import com.devpilot.backend.common.exception.exceptions.TaskNotFoundException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.member.repository.MemberRepository
import com.devpilot.backend.task.dto.TaskCreateRequest
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.dto.TaskUpdateRequest
import com.devpilot.backend.task.entity.Task
import com.devpilot.backend.task.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val memberRepository: MemberRepository,
) {

    fun createTask(userId: Long, req: TaskCreateRequest): TaskResponse {
        val member = memberRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        val task = Task(
            title = req.title,
            description = req.description,
            tags = req.tags,
            priority = req.priority,
            dueDate = req.dueDate,
            estimatedTimeHours = req.estimatedTimeHours,
            member = member
        )

        return taskRepository.save(task).toResponse()
    }

    fun getTask(userId: Long, id: Long): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(id, userId)
            ?: throw TaskNotFoundException()
        return task.toResponse()
    }

    fun getAllTasks(userId: Long): List<TaskResponse> {
        return taskRepository.findAllByMemberId(userId)
            .map { it.toResponse() }
    }

    @Transactional
    fun updateTask(userId: Long, id: Long, req: TaskUpdateRequest): TaskResponse {
        val task = taskRepository.findByIdAndMemberId(id, userId)
            ?: throw TaskNotFoundException()

        req.title?.let { task.title = it }
        req.description?.let { task.description = it }
        req.status?.let { task.status = it }
        req.tags?.let { task.tags = it }
        req.priority?.let { task.priority = it }
        req.dueDate?.let { task.dueDate = it }
        req.estimatedTimeHours?.let { task.estimatedTimeHours = it }
        req.actualTimeHours?.let { task.actualTimeHours = it }

        return task.toResponse()
    }

    fun deleteTask(userId: Long, id: Long) {
        val task = taskRepository.findByIdAndMemberId(id, userId)
            ?: throw TaskNotFoundException()
        taskRepository.delete(task)
    }

    private fun Task.toResponse() = TaskResponse(
        id = id,
        title = title,
        description = description,
        status = status,
        tags = tags,
        priority = priority,
        dueDate = dueDate,
        estimatedTimeHours = estimatedTimeHours,
        actualTimeHours = actualTimeHours
    )
}
