package kspt.pizzaservicespring.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import kspt.pizzaservicespring.models.User
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthorizationFilter(
        authenticationManager: AuthenticationManager
) : BasicAuthenticationFilter(authenticationManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authentication = getAuthentication(request)
        if (authentication != null) {
            SecurityContextHolder.getContext().authentication = authentication
        }
        chain.doFilter(request, response)
    }

    private fun Jws<Claims>.parseTokenParams(): UserTokenParams? {
        val id = body["id"] as? Int
        val login = body["login"] as? String
        val role = body["role"] as? String
        if (id == null || login == null || role == null) return null
        return UserTokenParams(id, login, role)
    }

    private fun retrieveUser(params: UserTokenParams): User? = User.repository.findByIdOrNull(params.id)

    private fun getAuthentication(request: HttpServletRequest): User? {
        val token = request.getHeader(SecurityConstants.TOKEN_HEADER) ?: return null
        if (token.isEmpty() || !token.startsWith(SecurityConstants.TOKEN_PREFIX)) return null

        try {
            val signingKey = SecurityConstants.JWT_SECRET.toByteArray()
            val parsedToken = Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .parseTokenParams()
            if (parsedToken != null) return retrieveUser(parsedToken)

        } catch (ex: Exception) {
            logger.warn("Unable authenticate user", ex)
        }

        return null
    }

}
