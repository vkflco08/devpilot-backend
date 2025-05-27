package com.devpilot.backend.project.entity

import BaseEntity
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.task.entity.Task
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import lombok.NoArgsConstructor

@Entity
@Table(name = "projects")
@NoArgsConstructor
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableList<Task> = mutableListOf()
): BaseEntity() {
    fun toResponse(): ProjectResponse {
        return ProjectResponse(
            id = this.id,
            name = this.name,
            description = this.description,
            tasks = this.tasks.map { task -> task.toResponse() },
            createdDate = createdDate,
            lastModifiedDate = lastModifiedDate,
        )
    }
}