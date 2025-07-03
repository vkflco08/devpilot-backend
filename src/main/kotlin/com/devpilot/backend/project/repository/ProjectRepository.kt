package com.devpilot.backend.project.repository

import com.devpilot.backend.project.entity.Project
import com.devpilot.backend.project.enum.ProjectStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProjectRepository : JpaRepository<Project, Long> {

    // 멤버 ID 기준으로 해당하는 모든 태스크 조회
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.member.id = :memberId")
    fun findAllByMemberIdWithTasks(@Param("memberId") memberId: Long): List<Project>

    // 멤버 ID 기준으로 ACTIVE 상태의 프로젝트와 해당 태스크들을 한번에 조회
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.member.id = :memberId and p.status = :status")
    fun findAllByMemberIdAndStatusWithTasks(@Param("memberId") memberId: Long, @Param("status") status: ProjectStatus = ProjectStatus.ACTIVE): List<Project>

    fun findByIdAndMemberId(projectId: Long?, userId: Long): Project?
}
