package com.devpilot.agent.project.service

import com.devpilot.backend.project.dto.ProjectRequest
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.project.dto.ProjectWithStatusResponse
import com.devpilot.backend.project.service.ProjectService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AgentProjectService(
    private val projectService: ProjectService
) {
    /**
     * 새로운 프로젝트를 생성합니다. (LLM 도구: create_project)
     */
    fun createAgentProject(userId: Long, request: ProjectRequest): ProjectResponse {
        return projectService.createNewProject(request, userId)
    }

    /**
     * 현재 사용자의 모든 프로젝트와 그에 속한 태스크들을 조회합니다. (LLM 도구: get_all_projects_with_tasks)
     */
    @Transactional(readOnly = true)
    fun getAllAgentProjectsWithTasks(userId: Long): List<ProjectWithStatusResponse> {
        return projectService.getMypageProjects(userId)
    }

    /**
     * 현재 사용자의 진행 중인 프로젝트들을 조회합니다. (LLM 도구: get_dashboard_projects)
     */
    @Transactional(readOnly = true)
    fun getDashboardProjects(userId: Long): List<ProjectResponse> {
        return projectService.getDashboardProjects(userId)
    }

    /**
     * 특정 ID의 단일 프로젝트와 그에 속한 태스크들을 조회합니다. (LLM 도구: get_single_project_with_tasks)
     */
    @Transactional(readOnly = true)
    fun getSingleAgentProjectWithTasks(userId: Long, projectId: Long): ProjectResponse {
        return projectService.getTask(userId, projectId)
    }

    /**
     * 프로젝트 정보를 업데이트합니다. (LLM 도구: update_project)
     */
    fun updateAgentProject(userId: Long, projectId: Long, request: ProjectRequest): ProjectResponse {
        return projectService.updateProject(userId, projectId, request)
    }

    /**
     * 프로젝트를 삭제합니다. (LLM 도구: delete_project)
     */
    fun deleteAgentProject(userId: Long, projectId: Long) {
        projectService.deleteProject(userId, projectId)
    }
}
