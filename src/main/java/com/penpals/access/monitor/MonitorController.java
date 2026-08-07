package com.penpals.access.monitor;

import com.penpals.common.ApiResponses;
import com.penpals.users.AppUser;
import com.penpals.users.AppUserService;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreateAppUserRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/penpal/monitors")
@PreAuthorize("hasAnyRole('MONITOR', 'ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class MonitorController {

	private final PenpalService penpalService;
	private final AppUserService appUserService;

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@PostMapping("/penpals")
	public ResponseEntity<Void> create(@Valid @RequestBody CreatePenpalRequest body) {
		Penpal created = penpalService.createPenpal(body);
		return ApiResponses.created(created.getId());
	}

	@PostMapping("/parent-helpers")
	public ResponseEntity<Void> create(@Valid @RequestBody CreateAppUserRequest body) {
		AppUser created = appUserService.createParentHelper(body);
		return ApiResponses.created(created.getId());
	}

	///////////////////////////////////////////////////////////////
	// READ

	@GetMapping("/penpals/{id}")
	public PenpalAdminView viewPenpal(@PathVariable Long id) {
		return PenpalAdminView.of(penpalService.findById(id));
	}

	@GetMapping("/parent-helpers/{id}")
	public UserFullView viewParentHelper(@PathVariable Long id) {
		return UserFullView.of(appUserService.findByIdWithRole(id, RoleEnum.PARENT_HELPER));
	}

	@GetMapping("/monitors/{id}")
	public UserFullView viewMonitor(@PathVariable Long id) {
		return UserFullView.of(appUserService.findByIdWithRole(id, RoleEnum.MONITOR));
	}

	@GetMapping("/admins/{id}")
	public UserFullView viewAdmin(@PathVariable Long id) {
		return UserFullView.of(appUserService.findByIdWithRole(id, RoleEnum.ADMIN));
	}

	@GetMapping("/relations")
	public List<MonitorMapRelationshipView> relations() {
		return penpalService.monitorChatMap();
	}

	@GetMapping("/all-users")
	public List<UserFullView> allUsers() {
		return appUserService.findAllOrderedByRole().stream()
			.map(UserFullView::of)
			.toList();
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@PutMapping("/reassign-penpal/{id}")
	public ResponseEntity<Void> reassignPenpal(@PathVariable Long id, @Valid @RequestBody CreatePenpalRequest body) {
		penpalService.reassignPenpal(id, body);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/penpals/{id}")
	public ResponseEntity<Void> updatePenpal(@PathVariable Long id, @Valid @RequestBody CreatePenpalRequest body) {
		penpalService.updatePenpalForMonitor(id, body);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/parent-helpers/{id}")
	public ResponseEntity<Void> updateParentHelper(@PathVariable Long id, @Valid @RequestBody CreateAppUserRequest body) {
		appUserService.update(id, body);
		return ResponseEntity.noContent().build();
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	///////////////////////////////////////////////////////////////
	// READ

	///////////////////////////////////////////////////////////////
	// UPDATE
}