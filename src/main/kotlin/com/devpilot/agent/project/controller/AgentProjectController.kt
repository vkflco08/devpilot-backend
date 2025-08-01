package com.devpilot.agent.project.controller

import com.devpilot.agent.project.service.AgentProjectService
import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.project.dto.ProjectRequest
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.project.dto.ProjectWithStatusResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agent/projects")
class AgentProjectController(
    private val agentProjectService: AgentProjectService
) {
    /**
     * 새로운 프로젝트 생성 (LLM 도구: create_project)
     * POST /api/agent/projects/new
     */
    @PostMapping("/new")
    fun createAgentProject(
        @RequestHeader("X-User-ID") userId: Long,
        @Valid @RequestBody request: ProjectRequest
    ): BaseResponse<ProjectResponse> {
        val response = agentProjectService.createAgentProject(userId, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 현재 사용자의 모든 프로젝트와 그에 속한 태스크 조회 (LLM 도구: get_all_projects_with_tasks)
     * GET /api/agent/projects/mypage
     */
    @GetMapping("/mypage")
    fun getAllAgentProjectsWithTasks(@RequestHeader("X-User-ID") userId: Long): BaseResponse<List<ProjectWithStatusResponse>> {
        val response = agentProjectService.getAllAgentProjectsWithTasks(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 현재 사용자의 진행 중인 프로젝트 조회 (LLM 도구: get_dashboard_projects)
     * GET /api/agent/projects/dashboard
     */
    @GetMapping("/dashboard")
    fun getDashboardProjects(@RequestHeader("X-User-ID") userId: Long): BaseResponse<List<ProjectResponse>> {
        val response = agentProjectService.getDashboardProjects(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 특정 ID의 단일 프로젝트와 그에 속한 태스크 조회 (LLM 도구: get_single_project_with_tasks)
     * GET /api/agent/projects/{projectId}
     */
    @GetMapping("/{projectId}")
    fun getSingleAgentProjectWithTasks(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable projectId: Long
    ): BaseResponse<ProjectResponse> {
        val response = agentProjectService.getSingleAgentProjectWithTasks(userId, projectId)
        return BaseResponse.success(data = response)
    }

    /**
     * 프로젝트 정보 수정 (LLM 도구: update_project)
     * PUT /api/agent/projects/{projectId}
     */
    @PutMapping("/{projectId}")
    fun updateAgentProject(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectRequest
    ): BaseResponse<ProjectResponse> {
        val response = agentProjectService.updateAgentProject(userId, projectId, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 프로젝트 삭제 (LLM 도구: delete_project)
     * DELETE /api/agent/projects/{projectId}
     */
    @DeleteMapping("/{projectId}")
    fun deleteAgentProject(
        @RequestHeader("X-User-ID") userId: Long,
        @PathVariable projectId: Long
    ): BaseResponse<Unit> {
        val response = agentProjectService.deleteAgentProject(userId, projectId)
        return BaseResponse.success(data = response)
    }
}
