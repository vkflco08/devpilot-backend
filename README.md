# 🚀 DevPilot Backend

DevPilot 백엔드는 프로젝트 및 작업 관리 기능을 제공하는 애플리케이션의 핵심 서비스입니다. <br>
Spring Boot와 Kotlin으로 개발되었으며, LLM 에이전트 통합을 통해 지능형 기능을 제공합니다.

## ✨ 주요 기능

- **RESTful API**: 프런트엔드와의 통신을 위한 API
- **사용자 관리**: JWT 기반 인증, OAuth2.0 소셜 로그인(Google, Kakao)
- **프로젝트 관리**: CRUD, 상태 관리, 멤버 관리
- **작업 관리**: CRUD, 상태/일정/태그 관리, 시간 추적
- **LLM 에이전트**: 자동화 작업, 스마트 추천

## 🛠 기술 스택

### 백엔드
- **프레임워크**: Spring Boot 3.x
- **언어**: Kotlin 1.8+
- **데이터베이스**: MySQL 8.0+
- **보안**: Spring Security 6.x + JWT
- **API 문서화**: SpringDoc OpenAPI 3.0

### 인프라
- **컨테이너화**: Docker
- **CI/CD**: Jenkins + Kubernetes
- **코드 품질**: SonarQube

## 📂 프로젝트 구조
```
src/main/kotlin/com/devpilot/backend/
├── agent/        # LLM 에이전트 기능
├── member/       # 사용자 계정 관리
├── project/      # 프로젝트 관리
└── task/         # 작업 관리
```

---

<div align="center">
  <img src="https://avatars.githubusercontent.com/u/67574367?s=150&v=4" alt="조승빈" width="100">
  <br>
  🔗 <a href="https://github.com/vkflco08">GitHub</a>
</div>
