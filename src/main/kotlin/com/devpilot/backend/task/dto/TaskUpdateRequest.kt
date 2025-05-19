package com.devpilot.backend.task.dto

import com.devpilot.backend.task.entity.TaskStatus
import java.time.LocalDateTime

data class TaskUpdateRequest(
    val title: String?,
    val description: String?,
    val status: TaskStatus?,
    val tags: String?,
    val priority: Int?,
    val dueDate: LocalDateTime?,
    val estimatedTimeHours: Double?,
    val actualTimeHours: Double?
)