package com.devpilot.backend.task.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class TaskTagUpdateRequest (
    @field:NotEmpty
    val tags: List<@NotBlank String>
)