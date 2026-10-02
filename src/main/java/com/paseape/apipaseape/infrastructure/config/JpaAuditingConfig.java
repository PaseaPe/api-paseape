package com.paseape.apipaseape.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    private static final String SISTEMA_AUDITOR = "SISTEMA";

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 1. Escenario No Autenticado: Schedulers (keepalive Aiven), migraciones o endpoints públicos
            if (authentication == null ||
                    !authentication.isAuthenticated() ||
                    authentication instanceof AnonymousAuthenticationToken) {
                return Optional.of(SISTEMA_AUDITOR);
            }

            // 2. Escenario con UserDetails propio de la aplicación
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                String username = userDetails.getUsername();
                if (username != null && username.contains("@")) {
                    return Optional.of(username.trim().toLowerCase());
                }
            }

            // 3. Escenario Principal como String directo (inyectado por el JwtFilter con el correo de Gmail)
            if (principal instanceof String principalString && principalString.contains("@")) {
                return Optional.of(principalString.trim().toLowerCase());
            }

            // 4. Fallback sobre authentication.getName() si tiene formato de email
            String name = authentication.getName();
            if (name != null && name.contains("@")) {
                return Optional.of(name.trim().toLowerCase());
            }

            return Optional.of(SISTEMA_AUDITOR);
        };
    }
}