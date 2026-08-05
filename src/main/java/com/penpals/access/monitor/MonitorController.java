package com.penpals.access.monitor;

import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.RelationshipsView;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.PenpalService;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	@PostMapping("/penpals")
	public UserSummaryView create(@RequestBody CreatePenpalRequest body) {
		return UserSummaryView.of(penpalService.createPenpal(body));
	}

	@GetMapping("/penpals/{id}")
	public PenpalBioView view(@PathVariable Long id) {
		return PenpalBioView.of(penpalService.findById(id));
	}

	@GetMapping("/relations")
	public List<MonitorMapRelationshipView> relations() {
		return penpalService.monitorChatMap();
	}
}