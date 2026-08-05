package com.penpals.common.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.penpals.access.CurrentUserService;
import com.penpals.users.RoleEnum;
import com.penpals.users.penpal.PenpalRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("dev")
public class DevSecurityConfig {

	@Bean
	SecurityFilterChain devFilterChain(HttpSecurity http, CurrentUserService currentUserService, PenpalRepository penpalRepository) throws Exception {
		var acting = new ActingAsPenpalFilter(currentUserService, penpalRepository);
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html",
					"/v3/api-docs/**", "/h2-console/**").permitAll()
				.requestMatchers("/api/penpal/admins/**").hasRole(RoleEnum.ADMIN.name())
				.anyRequest().authenticated())
			.httpBasic(withDefaults())
			.csrf(csrf -> csrf.disable())
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
			.addFilterAfter(acting, BasicAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	InMemoryUserDetailsManager users(PasswordEncoder encoder) {
		UserDetails penpal  = User.withUsername("penpal").password(encoder.encode("penpal")).roles(RoleEnum.PENPAL.name()).build();
		UserDetails parent_helper  = User.withUsername("parent_helper").password(encoder.encode("parent_helper")).roles(RoleEnum.PARENT_HELPER.name()).build();
		UserDetails monitor = User.withUsername("monitor").password(encoder.encode("monitor")).roles(RoleEnum.MONITOR.name()).build();
		UserDetails admin   = User.withUsername("admin").password(encoder.encode("admin")).roles(RoleEnum.ADMIN.name()).build();
		return new InMemoryUserDetailsManager(penpal, parent_helper, monitor, admin);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}