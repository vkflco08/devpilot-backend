package com.devpilot.backend.project.repository

import com.devpilot.backend.project.entity.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProjectRepository : JpaRepository<Project, Long> {
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.member.id = :memberId")
    fun findAllByMemberIdWithTasks(@Param("memberId") memberId: Long): List<Project>

    fun findByIdAndMemberId(projectId: Long?, userId: Long): Project?
}
