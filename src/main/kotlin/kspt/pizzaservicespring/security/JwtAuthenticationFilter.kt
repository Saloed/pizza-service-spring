package kspt.pizzaservicespring.security


import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kspt.pizzaservicespring.models.User
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.Date

private const val validityInMs = 36_000_00 * 10 // 10 hours

data class UserTokenParams(val id: Int, val login: String, val role: String)

data class UserCredentials(val username: String, val password: String)


class JwtAuthenticationFilter(
        private val myAuthenticationManager: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {
    init {
        setFilterProcessesUrl(SecurityConstants.AUTH_LOGIN_URL)
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val userCredentials = try{
            ObjectMapper().readValue(request.inputStream, UserCredentials::class.java)
        } catch (ex: JsonProcessingException){
            logger.warn("No credentials passed")
            throw BadCredentialsException("Np credentials supplied")
        }
        val authenticationToken = UsernamePasswordAuthenticationToken(userCredentials.username, userCredentials.password)
        return myAuthenticationManager.authenticate(authenticationToken)
    }

    private fun JwtBuilder.addTokenParams(params: UserTokenParams) = claim("id", params.id)
            .claim("login", params.login)
            .claim("role", params)


    override fun successfulAuthentication(
            request: HttpServletRequest,
            response: HttpServletResponse,
            chain: FilterChain,
            authResult: Authentication
    ) {
        val user = authResult.principal as User
        val signingKey = SecurityConstants.JWT_SECRET.toByteArray()
        val tokenParams = UserTokenParams(user.id, user.login, user.role.name)
        val token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
                .setHeaderParam("typ", SecurityConstants.TOKEN_TYPE)
                .setIssuer(SecurityConstants.TOKEN_ISSUER)
                .setAudience(SecurityConstants.TOKEN_AUDIENCE)
                .setSubject("Authentication")
                .setExpiration(getExpiration())
                .addTokenParams(tokenParams)
                .compact()
        response.addHeader(SecurityConstants.TOKEN_HEADER, SecurityConstants.TOKEN_PREFIX + token)
    }

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}
