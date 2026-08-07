package com.penpals.access.admin;

import com.penpals.common.ApiResponses;
import com.penpals.users.AppUser;
import com.penpals.users.AppUserService;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreateAppUserRequest;
import com.penpals.users.dto.PenpalViews.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penpal/admins")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

	private final AppUserService appUserService;

	@PostMapping("/monitors")
	public ResponseEntity<Void> create(@Valid @RequestBody CreateAppUserRequest body) {
		AppUser created = appUserService.createMonitor(body);
		return ApiResponses.created(created.getId());
	}
}