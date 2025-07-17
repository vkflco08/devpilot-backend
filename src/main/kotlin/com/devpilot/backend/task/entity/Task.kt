package com.devpilot.backend.task.entity

import BaseEntity
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.project.entity.Project
import com.devpilot.backend.task.dto.TaskResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "tasks")
data class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    var title: String,

    var description: String? = null,

    @Enumerated(EnumType.STRING)
    var status: TaskStatus = TaskStatus.TODO,

    var tags: String? = null, // comma-separated tags

    var priority: Int? = 3, // 1 (high) - 5 (low)

    var dueDate: LocalDate? = null,

    var estimatedTimeHours: Double? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Task? = null,

    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL], orphanRemoval = true)
    val subTasks: MutableList<Task> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    var project: Project? = null,

    @Enumerated(EnumType.STRING)
    var previousStatus: TaskStatus? = null,
): BaseEntity() {
    fun toResponse() = TaskResponse(
        id = id,
        projectId = project?.id,
        title = title,
        description = description,
        status = status,
        tags = tags,
        priority = priority,
        dueDate = dueDate,
        parentId = parent?.id,
        estimatedTimeHours = estimatedTimeHours,
        createdDate = createdDate,
        lastModifiedDate = lastModifiedDate,
        previousStatus = previousStatus
    )
}

enum class TaskStatus {
    TODO, DOING, DONE, BLOCKED
}
