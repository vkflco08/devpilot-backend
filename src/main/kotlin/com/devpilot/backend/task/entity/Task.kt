package com.devpilot.backend.task.entity

import com.devpilot.backend.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

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

    var priority: Int? = null, // 1 (high) - 5 (low)

    var dueDate: LocalDateTime? = null,

    var estimatedTimeHours: Double? = null,

    var actualTimeHours: Double? = null
)

enum class TaskStatus {
    TODO, DOING, DONE
}
