package com.devpilot.backend.task.dto

import com.devpilot.backend.task.entity.TaskStatus
import java.time.LocalDate

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val tags: String?,
    val priority: Int?,
    val dueDate: LocalDate?,
    val estimatedTimeHours: Double?,
)
