/*package com.medicinedonation.config;

import com.medicinedonation.security.JwtFilter;
import com.medicinedonation.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Disable CSRF — using JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Set session to stateless — JWT handles sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure endpoint access
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints — no token needed
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        // Admin only
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Doctor only
                        .requestMatchers("/api/doctor/**")
                        .hasRole("DOCTOR")

                        // Pharmacist only
                        .requestMatchers("/api/pharmacist/**")
                        .hasRole("PHARMACIST")

                        // Donor only
                        .requestMatchers("/api/donor/**")
                        .hasRole("DONOR")

                        // Recipient only
                        .requestMatchers("/api/recipient/**")
                        .hasRole("RECIPIENT")

                        // Collection point only
                        .requestMatchers("/api/collection-point/**")
                        .hasRole("COLLECTION_POINT")

                        // Medicine search — recipient and donor can access
                        .requestMatchers("/api/medicines/**")
                        .hasAnyRole("RECIPIENT", "DONOR", "ADMIN",
                                "DOCTOR", "PHARMACIST", "COLLECTION_POINT")

                        // Dashboard — all authenticated users
                        .requestMatchers("/api/dashboard/**")
                        .authenticated()

                        // Chatbot — all authenticated users
                        .requestMatchers("/api/chatbot/**")
                        .authenticated()

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before username/password filter
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)

                // Set authentication provider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}*/

/*package com.medicinedonation.config;

import com.medicinedonation.security.JwtFilter;
import com.medicinedonation.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ✅ Public — no token needed
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register/donor",
                                "/api/auth/register/recipient"
                        ).permitAll()

                        // Admin only
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Doctor only
                        .requestMatchers("/api/doctor/**")
                        .hasRole("DOCTOR")

                        // Pharmacist only
                        .requestMatchers("/api/pharmacist/**")
                        .hasRole("PHARMACIST")

                        // Donor only
                        .requestMatchers("/api/donor/**")
                        .hasRole("DONOR")

                        // Recipient only
                        .requestMatchers("/api/recipient/**")
                        .hasRole("RECIPIENT")

                        // Collection point only
                        .requestMatchers("/api/collection-point/**")
                        .hasRole("COLLECTION_POINT")

                        // Medicine — all authenticated
                        .requestMatchers("/api/medicines/**")
                        .hasAnyRole("RECIPIENT", "DONOR",
                                "ADMIN", "DOCTOR",
                                "PHARMACIST", "COLLECTION_POINT")

                        // Dashboard — all authenticated
                        .requestMatchers("/api/dashboard/**")
                        .authenticated()

                        // Chatbot — all authenticated
                        .requestMatchers("/api/chatbot/**")
                        .authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}*/
package com.medicinedonation.config;

import com.medicinedonation.security.JwtFilter;
import com.medicinedonation.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ✅ Public — no token needed
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register/donor",
                                "/api/auth/register/recipient"
                        ).permitAll()

                        // ✅ Collection points — ALL authenticated roles
                        // MUST be before /api/recipient/** rule
                        .requestMatchers("/api/recipient/collection-points")
                        .authenticated()

                        // Admin only
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Doctor only
                        .requestMatchers("/api/doctor/**")
                        .hasRole("DOCTOR")

                        // Pharmacist only
                        .requestMatchers("/api/pharmacist/**")
                        .hasRole("PHARMACIST")

                        // Donor only
                        .requestMatchers("/api/donor/**")
                        .hasRole("DONOR")

                        // Recipient only
                        .requestMatchers("/api/recipient/**")
                        .hasRole("RECIPIENT")

                        // Collection point only
                        .requestMatchers("/api/collection-point/**")
                        .hasRole("COLLECTION_POINT")

                        // Medicine — all authenticated
                        .requestMatchers("/api/medicines/**")
                        .hasAnyRole("RECIPIENT", "DONOR",
                                "ADMIN", "DOCTOR",
                                "PHARMACIST", "COLLECTION_POINT")

                        // Dashboard — all authenticated
                        .requestMatchers("/api/dashboard/**")
                        .authenticated()

                        // Chatbot — all authenticated
                        .requestMatchers("/api/chatbot/**")
                        .authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)

                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
