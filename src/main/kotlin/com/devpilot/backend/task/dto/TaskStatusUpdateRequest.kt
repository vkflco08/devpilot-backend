package com.devpilot.backend.task.dto

import com.devpilot.backend.task.entity.TaskStatus
import jakarta.validation.constraints.NotNull

class TaskStatusUpdateRequest (
    @field:NotNull
    val status: TaskStatus
)