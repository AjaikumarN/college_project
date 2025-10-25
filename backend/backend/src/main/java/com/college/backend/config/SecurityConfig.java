package com.college.backend.config;

import com.college.backend.security.JwtAuthenticationEntryPoint;
import com.college.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Faculty endpoints
                .requestMatchers("/api/faculty/**").hasAnyRole("FACULTY", "ADMIN")
                
                // Student endpoints
                .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
                
                // Department endpoints (admin and faculty can access)
                .requestMatchers("/api/departments/**").hasAnyRole("FACULTY", "ADMIN")
                
                // Course endpoints
                .requestMatchers("/api/courses/create").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/api/courses/update/**").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/api/courses/delete/**").hasRole("ADMIN")
                .requestMatchers("/api/courses/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
                
                // Grade endpoints
                .requestMatchers("/api/grades/create").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/api/grades/update/**").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/api/grades/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
                
                // Attendance endpoints
                .requestMatchers("/api/attendance/mark").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/api/attendance/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
                
                // Enrollment endpoints
                .requestMatchers("/api/enrollments/enroll").hasAnyRole("STUDENT", "ADMIN")
                .requestMatchers("/api/enrollments/approve/**").hasAnyRole("FACULTY", "ADMIN")
                .requestMatchers("/api/enrollments/**").hasAnyRole("STUDENT", "FACULTY", "ADMIN")
                
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}