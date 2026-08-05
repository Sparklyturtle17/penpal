package com.penpals.access.parenthelper;

import com.penpals.access.CurrentUserService;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.PenpalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/penpal/penpals")
@PreAuthorize("hasAnyRole('PENPAL')")
@Slf4j
@RequiredArgsConstructor
public class PenpalController {

	private final CurrentUserService currentUserService;
	private final PenpalService penpalService;

	@GetMapping("/relations")
	public PenpalMapRelationshipView relations (@RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		return penpalService.penpalChatMap(penpalId);
	}
}
