package com.penpals.access.parenthelper;

import com.penpals.access.CurrentUserService;
import com.penpals.common.ApiResponses;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.Penpal;
import com.penpals.users.penpal.PenpalService;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penpal/parent-helpers")
@PreAuthorize("hasAnyRole('PARENT_HELPER')")
@Slf4j
@RequiredArgsConstructor
public class ParentHelperController {

	private final PenpalService penpalService;
	private final CurrentUserService currentUserService;

	@PostMapping("/my-penpals")
	public ResponseEntity<Void> create(@Valid @RequestBody CreatePenpalRequest body) {
		Penpal created = penpalService.createPenpalForGuardian(body, currentUserService.currentId());
		return ApiResponses.created(created.getId());
	}

	@GetMapping("/my-penpals-companions")
	public GuardianMapRelationshipView relations() {
		return penpalService.guardianChatMap(currentUserService.currentId());
	}

	@GetMapping("/my-penpals/{id}")
	public PenpalAdminView view(@PathVariable Long id) {
		return PenpalAdminView.of(penpalService.findByIdForGuardian(id, currentUserService.currentId()));
	}

	@GetMapping("/my-penpals-companions/{id}")
	public PenpalBioView viewCompanion(@PathVariable Long id) {
		return PenpalBioView.of(penpalService.findByIdForCompanionGuardian(id, currentUserService.currentId()));
	}

	@PutMapping("/my-penpals/{id}")
	public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody CreatePenpalRequest body) {
		penpalService.updatePenpalForGuardian(id, body, currentUserService.currentId());
		return ResponseEntity.noContent().build();
	}

}