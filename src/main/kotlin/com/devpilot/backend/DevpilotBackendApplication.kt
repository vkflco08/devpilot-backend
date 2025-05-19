package com.devpilot.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DevpilotBackendApplication

fun main(args: Array<String>) {
	runApplication<DevpilotBackendApplication>(*args)
}
