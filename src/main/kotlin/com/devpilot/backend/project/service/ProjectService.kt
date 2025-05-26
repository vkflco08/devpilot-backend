package com.devpilot.backend.project.service

import com.devpilot.backend.common.exception.exceptions.ProjectNotFoundException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.member.repository.MemberRepository
import com.devpilot.backend.project.dto.ProjectRequest
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.project.entity.Project
import com.devpilot.backend.project.repository.ProjectRepository
import com.devpilot.backend.task.dto.TaskResponse
import com.devpilot.backend.task.repository.TaskRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val memberRepository: MemberRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository
) {

    fun getAllTasks(userId: Long): List<ProjectResponse> {
        val findProjects: List<Project> = projectRepository.findAllByMemberIdWithTasks(userId)
        if(findProjects.isEmpty()) throw ProjectNotFoundException()
        return findProjects.map { project ->
            ProjectResponse(
                id = project.id,
                name = project.name,
                description = project.description,
                tasks = project.tasks.map { task ->
                    TaskResponse(
                        id = task.id,
                        projectId = task.project?.id,
                        title = task.title,
                        description = task.description,
                        status = task.status,
                        tags = task.tags,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        estimatedTimeHours = task.estimatedTimeHours,
                        createdDate = task.createdDate,
                        lastModifiedDate = task.lastModifiedDate
                    )
                }
            )
        }
    }

    @Transactional
    fun createNewProject(projectRequest: ProjectRequest, userId: Long): ProjectResponse {
        val member = memberRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        val project = Project(
            name = projectRequest.projectName,
            description = projectRequest.projectDescription,
            member = member,
        )

        val savedProject = projectRepository.save(project)

        return ProjectResponse(
            id = savedProject.id,
            name = savedProject.name,
            description = savedProject.description,
            tasks = listOf()  // 생성 직후엔 태스크 없음
        )
    }

    fun getTask(userId: Long, projectId: Long): ProjectResponse {
        val project = projectRepository.findByIdAndMemberId(projectId, userId)
            ?: throw ProjectNotFoundException()

        return ProjectResponse(
            id = project.id,
            name = project.name,
            description = project.description,
            tasks = project.tasks.map { task ->
                TaskResponse(
                    id = task.id,
                    projectId = task.project?.id,
                    title = task.title,
                    description = task.description,
                    status = task.status,
                    tags = task.tags,
                    priority = task.priority,
                    dueDate = task.dueDate,
                    estimatedTimeHours = task.estimatedTimeHours,
                    createdDate = task.createdDate,
                    lastModifiedDate = task.lastModifiedDate
                )
            }
        )
    }
}
