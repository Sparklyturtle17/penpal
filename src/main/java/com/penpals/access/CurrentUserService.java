package com.penpals.access;

import com.penpals.users.AppUser;
import com.penpals.users.AppUserRepository;
import com.penpals.users.dto.CreateAppUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

	private final AppUserRepository appUserRepository;

	/**
	 * Dev (Basic): auth.getName() is the username -> matches seeded app_user.auth_id.
	 * Prod (JWT):  auth.getName() is the Auth0 "sub" -> matches auth_id, or links on first login.
	 */
	@Transactional
	public AppUser require(Authentication auth) {
		return appUserRepository.findByAuthId(auth.getName())
			.orElseGet(() -> linkIfJwt(auth));
	}

	private AppUser linkIfJwt(Authentication auth) {
		if (auth instanceof JwtAuthenticationToken jwtAuth) {   // prod first login
			return linkOnFirstLogin(jwtAuth.getToken());
		}
		// dev: user should already be seeded with a matching auth_id
		throw new IllegalStateException("No app_user linked to principal '" + auth.getName() + "'");
	}

	private AppUser linkOnFirstLogin(Jwt jwt) {
		String email = jwt.getClaimAsString("email");
		String whatsapp = jwt.getClaimAsString("whatsapp");

		AppUser user = null;
		if (email != null) user = appUserRepository.findByEmail(email).orElse(null);
		if (user == null && whatsapp != null) user = appUserRepository.findByWhatsapp(whatsapp).orElse(null);
		if (user == null) {
			throw new IllegalStateException("No app_user matches this login (sub=" + jwt.getSubject() + ")");
		}
		user.setAuthId(jwt.getSubject());
		return appUserRepository.save(user);
	}

	public AppUser current() {
		return require(SecurityContextHolder.getContext().getAuthentication());
	}

	public Long currentId() {
		return current().getId();
	}
}