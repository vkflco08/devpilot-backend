package com.devpilot.backend.common.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomSecurityUserDetails(
    val userId: Long?,
    userName: String,
    password: String,
    authorities: Collection<GrantedAuthority>,
) : User(userName, password, authorities)
