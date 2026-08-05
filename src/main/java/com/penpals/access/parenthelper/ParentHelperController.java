package com.penpals.access.parenthelper;

import com.penpals.access.CurrentUserService;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.PenpalService;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penpal/parent_helpers")
@PreAuthorize("hasAnyRole('PARENT_HELPER')")
@Slf4j
@RequiredArgsConstructor
public class ParentHelperController {

	private final PenpalService penpalService;
	private final CurrentUserService currentUserService;

	// controlling which data is returned based on logged in user id
	@GetMapping("/me")
	public UserFullView me() {
		return UserFullView.of(currentUserService.current());
	}

	@GetMapping("/relations")
	public GuardianMapRelationshipView relations() {
		return penpalService.guardianChatMap(currentUserService.currentId());
	}

	//todo parenthelper cannot be created by parent helper, and cannot be another parent helper
	// use current logged in user
	@PostMapping("/penpals")
	public UserSummaryView create(@RequestBody CreatePenpalRequest body) {
		return UserSummaryView.of(penpalService.createPenpal(body));
	}

	// todo check relationship first - duplicate methods, return admin view if I'm parent, bio view if companion of my child
	@GetMapping("/penpals/{id}")
	public PenpalBioView view(@PathVariable Long id) {
		return PenpalBioView.of(penpalService.findById(id));
	}
}