package com.penpals.users.dto;

import java.util.List;

public interface ChatMapView {

	record GuardianNode(
		Long guardianId,
		String guardianName,
		List<PenpalNode> penpals          // this guardian's kids — the "converge" point
	) {}

	record PenpalNode(
		Long penpalId,
		String penpalName,
		Companion companion               // who they're chatting with
	) {}

	record Companion(
		Long id,
		String name,
		Long guardianId,                  // the companion's own guardian
		String guardianName
	) {}
}