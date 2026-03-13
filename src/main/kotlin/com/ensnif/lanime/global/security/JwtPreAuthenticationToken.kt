package com.ensnif.lanime.global.security

import org.springframework.security.authentication.AbstractAuthenticationToken

class JwtPreAuthenticationToken(
    val accessToken: String,
    val profileToken: String?
) : AbstractAuthenticationToken(null) {
    override fun getCredentials(): Any = accessToken
    override fun getPrincipal(): Any = accessToken
}