package com.penpals.common.config;

import com.penpals.users.AppUser;
import com.penpals.access.CurrentUserService;
import com.penpals.users.RoleEnum;
import com.penpals.users.penpal.Penpal;
import com.penpals.users.penpal.PenpalRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class ActingAsPenpalFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Acting-As-Penpal";
	public static final String ACTIVE_PENPAL_ATTR = "actingAsPenpal";

	private final CurrentUserService currentUserService;
	private final PenpalRepository penpalRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {

		String header = request.getHeader(HEADER);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// No child selected, or not authenticated yet -> guardian keeps full authorities
		if (header == null || auth == null || !auth.isAuthenticated()) {
			chain.doFilter(request, response);
			return;
		}

		Long penpalId;
		try {
			penpalId = Long.valueOf(header.trim());
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid " + HEADER);
			return;
		}

		AppUser guardian = currentUserService.require(auth);
		Penpal penpal = penpalRepository.findById(penpalId).orElse(null);

		if (penpal == null || !guardianOwns(penpal, guardian)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not your penpal");
			return;
		}

		// DOWNSCOPE: keep guardian identity in the name, but authorities become ROLE_PENPAL only.
		var downscoped = new UsernamePasswordAuthenticationToken(
			auth.getName(),                                   // guardian's authId — identity preserved
			null,
			List.of(new SimpleGrantedAuthority(RoleEnum.PENPAL.getAuthority())));  // "ROLE_PENPAL"
		SecurityContextHolder.getContext().setAuthentication(downscoped);

		request.setAttribute(ACTIVE_PENPAL_ATTR, penpal.getId());   // who we're acting as
		chain.doFilter(request, response);
	}

	private boolean guardianOwns(Penpal penpal, AppUser guardian) {
		if (penpal.getParentHelper() != null
			&& penpal.getParentHelper().getId().equals(guardian.getId())) {
			return true;                                            // the penpal's parent/helper
		}
		return guardian.getRole() == RoleEnum.MONITOR              // staff can act as any penpal
			|| guardian.getRole() == RoleEnum.ADMIN;
	}
}