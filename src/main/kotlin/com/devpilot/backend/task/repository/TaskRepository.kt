package com.devpilot.backend.task.repository

import com.devpilot.backend.project.enum.ProjectStatus
import com.devpilot.backend.task.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByIdAndMemberId(id: Long, memberId: Long): Task?
    fun findAllByMemberId(memberId: Long): List<Task>
    fun findAllByProjectIdAndMemberId(projectId: Long, memberId: Long): List<Task>

    // 프로젝트가 ACTIVE 상태인 경우의 태스크만 조회
    @Query("""
    SELECT t FROM Task t
    WHERE t.project.member.id = :memberId
      AND t.project.status = :status
""")
    fun findAllByMemberIdAndProjectStatus(
        @Param("memberId") memberId: Long,
        @Param("status") status: ProjectStatus = ProjectStatus.ACTIVE
    ): List<Task>
}
