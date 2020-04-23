package com.raad.converter.config;

import com.raad.converter.filter.TokenAuthenticationFilter;
import com.raad.converter.model.service.CustomUserDetailsService;
import com.raad.converter.security.RestAuthenticationEntryPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * detail of EnableGlobalMethodSecurity
 * securedEnabled: It enables the @Secured annotation using which you can protect your controller/service methods
 * @Secured("ROLE_ADMIN"), @Secured({"ROLE_USER", "ROLE_ADMIN"})
 * jsr250Enabled: It enables the @RolesAllowed annotation that can be used like this
 * @RolesAllowed("ROLE_ADMIN")
 * prePostEnabled: It enables more complex expression based access control syntax with
 * @PreAuthorize and @PostAuthorize annotations
 * @PreAuthorize("isAnonymous()"), @PreAuthorize("hasRole('USER')")
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    public Logger logger = LogManager.getLogger(WebSecurityConfig.class);

    private final String LOGIN_PATH = "/auth/login";
    private final String SIGNUP_PATH = "/auth/signup";
    private final String LOGOUT_PATH = "/auth/logout";

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private CustomUserDetailsService jwtUserDetailsService;

    @Bean
    public TokenAuthenticationFilter jwtAuthenticationTokenFilter() throws Exception {
        return new TokenAuthenticationFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.jwtUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().authorizeRequests()
            .antMatchers(LOGIN_PATH, SIGNUP_PATH, LOGOUT_PATH).permitAll().anyRequest().authenticated()
            .and().exceptionHandling().authenticationEntryPoint(this.restAuthenticationEntryPoint)
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*");
    }
}
