package com.devpilot.backend.task.dto

import com.devpilot.backend.task.entity.TaskStatus
import java.time.LocalDate

data class TaskCreateRequest(
    val title: String,
    val description: String?,
    val tags: String?,
    val priority: Int?,
    val dueDate: LocalDate?,
    val estimatedTimeHours: Double?,
    val status: TaskStatus,
    val parentId: Long?,
    val projectId: Long? = null,
    val previousStatus: TaskStatus? = null,
)