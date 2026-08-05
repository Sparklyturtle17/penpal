package com.penpals.access;

import com.penpals.users.AppUserService;
import com.penpals.users.dto.AppUserViews.UserFullView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

	private final CurrentUserService currentUserService;
	private final AppUserService appUserService;

	// things every role can do to themselves

	@GetMapping("/me")
	public UserFullView me() {
		return UserFullView.of(currentUserService.current());
	}

}