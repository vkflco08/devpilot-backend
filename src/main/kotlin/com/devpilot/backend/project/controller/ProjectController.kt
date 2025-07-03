package com.devpilot.backend.project.controller

import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.project.dto.ProjectRequest
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.project.service.ProjectService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/project")
class ProjectController(
    private val projectService: ProjectService
) {
    /**
     * 프로젝트 생성
     */
    @PostMapping("/new")
    fun createProject(@RequestBody @Valid projectRequest: ProjectRequest): BaseResponse<ProjectResponse> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()

        val response = projectService.createNewProject(projectRequest, userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 모든 프로젝트와 태스크 조회
     */
    @GetMapping("/mypage")
    fun getMypageProjects(): BaseResponse<List<ProjectResponse>> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = projectService.getMypageProjects(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 진행중인 프로젝트와 태스크 조회
     */
    @GetMapping("/dashboard")
    fun getDashboardProjects(): BaseResponse<List<ProjectResponse>> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        val response = projectService.getDashboardProjects(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 단일 프로젝트 태스크 조회
     */
    @GetMapping("/{projectId}")
    fun getOne(@PathVariable projectId: Long): BaseResponse<ProjectResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
                ?: throw UserNotFoundException()

        val response = projectService.getTask(userId, projectId)
        return BaseResponse.success(data = response)
    }

    /**
     * 프로젝트 수정
     */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid request: ProjectRequest
    ): BaseResponse<ProjectResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
                ?: throw UserNotFoundException()

        val response = projectService.updateProject(userId, id, request)
        return BaseResponse.success(data = response)
    }

    /**
     * 프로젝트 삭제
     */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseResponse<Unit> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
                ?: throw UserNotFoundException()

        projectService.deleteProject(userId, id)
        return BaseResponse.success()
    }
}
