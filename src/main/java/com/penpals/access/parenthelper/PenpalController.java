package com.penpals.access.parenthelper;

import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.PenpalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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

	private final PenpalService penpalService;

	@GetMapping("/me")
	public PenpalBioView me(
		@RequestAttribute(name = ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR, required = false) Long penpalId) {
		if (penpalId == null) {
			throw new AccessDeniedException("App users other than penpal must use the default endpoint for /me.");
		}
		return PenpalBioView.of(penpalService.findById(penpalId));
	}

	@GetMapping("/relations")
	public PenpalMapRelationshipView relations (@RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		return penpalService.penpalChatMap(penpalId);
	}
}
