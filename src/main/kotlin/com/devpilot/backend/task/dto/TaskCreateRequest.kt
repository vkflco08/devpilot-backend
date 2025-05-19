package com.devpilot.backend.task.dto

import java.time.LocalDateTime

data class TaskCreateRequest(
    val title: String,
    val description: String?,
    val tags: String?,
    val priority: Int?,
    val dueDate: LocalDateTime?,
    val estimatedTimeHours: Double?
)