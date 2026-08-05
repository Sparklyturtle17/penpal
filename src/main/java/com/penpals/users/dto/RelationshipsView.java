package com.penpals.users.dto;

import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.AppUserViews.*;

import java.util.List;

public interface RelationshipsView {

	// MAP 1 — monitor: one per chat, both penpals admin (each nests its guardian)
	record MonitorMapRelationshipView (
		PenpalAdminView penpalOne,
		PenpalAdminView penpalTwo
	) {}

	// MAP 2 — parent/helper: guardian once, then their penpals (admin) each with a bio companion
	record GuardianMapRelationshipView (
		UserFullView guardian,
		List<PenpalWithCompanion> penpals
	) {}

	record PenpalWithCompanion(
		PenpalAdminView penpal,
		PenpalBioView companion
	) {}

	// MAP 3 — penpal: self + companion, both bio
	record PenpalMapRelationshipView (
		PenpalBioView self,
		PenpalBioView companion
	) {}
}