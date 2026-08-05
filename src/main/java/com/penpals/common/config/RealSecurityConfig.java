package com.penpals.common.config;

import com.penpals.access.CurrentUserService;
import com.penpals.users.penpal.PenpalRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!dev")
public class RealSecurityConfig {

	@Bean
	SecurityFilterChain apiFilterChain(HttpSecurity http, CurrentUserService currentUserService, PenpalRepository penpalRepository) throws Exception {
		var acting = new ActingAsPenpalFilter(currentUserService, penpalRepository);
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated())
			.oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(converter())))
			.csrf(csrf -> csrf.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilterAfter(acting, BearerTokenAuthenticationFilter.class);
		return http.build();
	}

	private JwtAuthenticationConverter converter() {
		JwtAuthenticationConverter c = new JwtAuthenticationConverter();
		c.setJwtGrantedAuthoritiesConverter(jwt -> {
			@SuppressWarnings("unchecked")
			List<String> roles = (List<String>) jwt.getClaims()
				.getOrDefault("https://penpals.example.com/roles", List.of());  // your Auth0 roles claim
			return roles.stream()
				.map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
				.toList();
		});
		return c;
	}
}