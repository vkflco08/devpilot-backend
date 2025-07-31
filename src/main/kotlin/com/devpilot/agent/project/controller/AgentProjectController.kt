package com.devpilot.backend.agent.task.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agent/project")
class AgentProjectController {
    @RequestMapping("/tasks/all")
    fun getAllTasks(){

    }
}