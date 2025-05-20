package com.devpilot.backend.task.repository

import com.devpilot.backend.task.entity.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByIdAndMemberId(projectId: Long?, userId: Long): Project?
}
