package com.devpilot.backend.project.dto

import com.devpilot.backend.task.dto.TaskResponse
import java.time.LocalDateTime

data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val tasks: List<TaskResponse>,
    val createdDate: LocalDateTime?,
    val lastModifiedDate: LocalDateTime?,
)
