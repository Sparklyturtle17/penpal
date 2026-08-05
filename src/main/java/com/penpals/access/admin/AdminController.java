package com.penpals.access.admin;

import com.penpals.access.CurrentUserService;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.penpal.PenpalService;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penpal/admins")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

	private final PenpalService penpalService;
	private final CurrentUserService currentUserService;

	@PostMapping("/penpals")
	public UserSummaryView create(@Valid @RequestBody CreatePenpalRequest body) {
		return UserSummaryView.of(penpalService.createPenpal(body));
	}

	@GetMapping("/penpals/{id}")
	public PenpalBioView view(@PathVariable Long id) {
		return PenpalBioView.of(penpalService.findById(id));
	}
}