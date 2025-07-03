package com.devpilot.backend.project.dto

import com.devpilot.backend.project.enum.ProjectStatus

data class ProjectRequest(
    val projectName: String,
    val projectDescription: String?,
    val projectStatus: ProjectStatus
)
