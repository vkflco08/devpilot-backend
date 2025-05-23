package com.devpilot.backend.project.controller

import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.CustomUser
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.project.dto.ProjectRequest
import com.devpilot.backend.project.dto.ProjectResponse
import com.devpilot.backend.project.service.ProjectService
import com.devpilot.backend.task.dto.TaskResponse
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
            ?: throw UserNotFoundException()

        val response = projectService.createNewProject(projectRequest, userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 전체 프로젝트와 태스크 조회
     */
    @GetMapping("/all")
    fun getAll(): BaseResponse<List<ProjectResponse>> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
            ?: throw UserNotFoundException()
        val response = projectService.getAllTasks(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 단일 프로젝트 태스크 조회
     */
    @GetMapping("/{projectId}")
    fun getOne(@PathVariable projectId: Long): BaseResponse<ProjectResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val response = projectService.getTask(userId, projectId)
        return BaseResponse.success(data = response)
    }
}