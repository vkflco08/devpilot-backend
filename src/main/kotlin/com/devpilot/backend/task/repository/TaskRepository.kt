package com.devpilot.backend.task.repository

import com.devpilot.backend.task.entity.Task
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long> {
    fun findByIdAndMemberId(id: Long, memberId: Long): Task?
    fun findAllByMemberId(memberId: Long): List<Task>
}
