plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("plugin.serialization") version "1.9.22"
	application
}

group = "com.devpilog"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("com.mysql:mysql-connector-j")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// jwt
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// api rate limit - bucket4j
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:8.0.1")

	implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("io.modelcontextprotocol:kotlin-sdk:0.3.0")
	implementation("com.squareup.okio:okio:3.7.0")

	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
	mainClass.set("com.devpilot.backend.DevpilotBackendApplicationKt")
}

tasks.register("runMcpServer") {
	group = "application"
	description = "Runs the MCP server"
	doLast {
		exec {
			workingDir = projectDir
			commandLine = listOf(
				"${projectDir}/gradlew",
				"run",
				"--args=com.devpilot.backend.mcp.McpTaskServerMainKt"
			)
		}
	}
}
