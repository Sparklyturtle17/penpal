package com.penpals.access.parenthelper;

import com.penpals.access.CurrentUserService;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.penpal.PenpalService;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penpal/parent_helpers")
@PreAuthorize("hasAnyRole('PARENT_HELPER')")
@Slf4j
@RequiredArgsConstructor
public class ParentHelperController {

	private final PenpalService penpalService;
	private final CurrentUserService currentUserService;

	@GetMapping("/me")
	public UserFullView me(Authentication auth) {
		return UserFullView.of(currentUserService.require(auth));
	}

	@PostMapping("/penpals")
	public UserSummaryView create(@RequestBody CreatePenpalRequest body) {
		return UserSummaryView.of(penpalService.createPenpal(body));
	}

	@GetMapping("/penpals/{id}")
	public PenpalViews.PenpalBioView view(@PathVariable Long id) {
		return PenpalViews.PenpalBioView.of(penpalService.findById(id));
	}
}