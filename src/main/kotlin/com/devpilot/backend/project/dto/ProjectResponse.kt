package com.devpilot.backend.project.dto

import com.devpilot.backend.task.dto.TaskResponse

data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val tasks: List<TaskResponse>
)
