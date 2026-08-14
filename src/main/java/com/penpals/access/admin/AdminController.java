package com.penpals.access.admin;

import com.penpals.chat.dto.AuditViews.*;
import com.penpals.chat.message.audit.MessageAuditService;
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
	private final MessageAuditService messageAuditService;

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@PostMapping("/monitors")
	public ResponseEntity<Void> create(@Valid @RequestBody CreateAppUserRequest body) {
		AppUser created = appUserService.createMonitor(body);
		return ApiResponses.created(created.getId());
	}

	///////////////////////////////////////////////////////////////
	// READ

	///////////////////////////////////////////////////////////////
	// UPDATE


	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	///////////////////////////////////////////////////////////////
	// READ

	@GetMapping("/audits/messages/all")
	public ListOfAudits getAuditsByMessage() {
		return ListOfAudits.of(messageAuditService.getAuditRecordsByMessage());
	}

	@GetMapping("/audits/message/{messageId}")
	public ListOfAudits getAuditsByMessageId(@PathVariable Long messageId) {
		return ListOfAudits.of(messageAuditService.getAuditRecordsByMessageId(messageId));
	}

	@GetMapping("/audits/user/{appUserId}")
	public ListOfAudits getAuditsTouchedByAppUser(@PathVariable Long appUserId) {
		return ListOfAudits.of(messageAuditService.getAuditRecordsTouchedByAppUserId(appUserId));
	}

	///////////////////////////////////////////////////////////////
	// UPDATE
}