package com.penpals.access;

import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.AppUserViews.UserFullView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

	private final CurrentUserService currentUserService;

	// things every role can do to themselves except penpals must use their own route

	@GetMapping("/me")
	public UserFullView me(
		@RequestAttribute(name = ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR, required = false) Long penpalId) {
		if (penpalId != null) {
			throw new AccessDeniedException("Penpals must use the penpal endpoint for /me.");
		}
		return UserFullView.of(currentUserService.current());
	}

}