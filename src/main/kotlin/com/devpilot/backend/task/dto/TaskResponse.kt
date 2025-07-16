package com.devpilot.backend.task.dto

import com.devpilot.backend.task.entity.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class TaskResponse(
    val id: Long,
    val projectId: Long?,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val tags: String?,
    val priority: Int?,
    val parentId: Long?,
    val dueDate: LocalDate?,
    val estimatedTimeHours: Double?,
    val createdDate: LocalDateTime?,
    val lastModifiedDate: LocalDateTime?,
)
