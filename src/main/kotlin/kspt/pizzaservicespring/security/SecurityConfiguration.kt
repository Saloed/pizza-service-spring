package kspt.pizzaservicespring.security

import kspt.pizzaservicespring.models.User
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.lang.IllegalStateException


@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(SecurityConstants.CLIENT_REGISTRATION_URL).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(JwtAuthenticationFilter(authenticationManager()))
                .addFilter(JwtAuthorizationFilter(authenticationManager()))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun authenticationManager() = AuthenticationManager { authentication ->
        val principal = authentication.principal
        if (principal is User) return@AuthenticationManager principal
        if (principal !is String) throw IllegalStateException("Unexpected principal: $principal")
        val login = authentication.principal as String
        val password = authentication.credentials as String

        val candidate = User.repository.findByLogin(login) ?: throw BadCredentialsException("No such user: $login")
        if (!passwordEncoder().matches(password, candidate.password)) {
            throw BadCredentialsException("Incorrect password")
        }
        candidate
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val corsConfiguration = CorsConfiguration().applyPermitDefaultValues()
        corsConfiguration.addAllowedMethod(HttpMethod.OPTIONS)
        corsConfiguration.addAllowedMethod(HttpMethod.PUT)
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }
}
