package com.devpilot.backend.project.service

import com.devpilot.backend.common.exception.exceptions.ProjectNotFoundException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.member.repository.MemberRepository
import com.devpilot.backend.project.dto.ProjectRequest
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.project.dto.ProjectWithStatusResponse
import com.devpilot.backend.project.entity.Project
import com.devpilot.backend.project.repository.ProjectRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val memberRepository: MemberRepository,
    private val projectRepository: ProjectRepository,
) {

    fun getMypageProjects(userId: Long): List<ProjectWithStatusResponse> {
        val findProjects: List<Project> = projectRepository.findAllByMemberIdWithTasks(userId)
        if(findProjects.isEmpty()) throw ProjectNotFoundException()
        return findProjects.map { project -> project.toWithStatusResponse() }
    }

    fun getDashboardProjects(userId: Long): List<ProjectResponse> {
        val findProjects: List<Project> = projectRepository.findAllByMemberIdAndStatusWithTasks(userId)
        if(findProjects.isEmpty()) throw ProjectNotFoundException()
        return findProjects.map { project -> project.toResponse() }
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

        return savedProject.toResponse()
    }

    fun getTask(userId: Long, projectId: Long): ProjectResponse {
        val project = projectRepository.findByIdAndMemberId(projectId, userId)
            ?: throw ProjectNotFoundException()

        return project.toResponse()
    }

    @Transactional
    fun updateProject(userId: Long, projectId: Long, request: ProjectRequest): ProjectResponse {
        val project = projectRepository.findByIdAndMemberId(projectId, userId)
            ?: throw ProjectNotFoundException()

        request.projectName.let { project.name = it }
        request.projectDescription.let { project.description = it }
        request.projectStatus.let { project.status = it }

        val updated = projectRepository.save(project)
        return updated.toResponse()
    }

    @Transactional
    fun deleteProject(userId: Long, id: Long) {
        val project = projectRepository.findByIdAndMemberId(id, userId)
            ?: throw ProjectNotFoundException()
        projectRepository.delete(project)
    }
}
