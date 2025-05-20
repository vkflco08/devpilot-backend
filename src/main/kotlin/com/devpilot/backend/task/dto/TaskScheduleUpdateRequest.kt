package com.devpilot.backend.task.dto

import java.time.LocalDate

class TaskScheduleUpdateRequest (
    val priority: Int?, // 우선순위는 선택적 (nullable)
    val dueDate: LocalDate? // 마감일도 선택적
)