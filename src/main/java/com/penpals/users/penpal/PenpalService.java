package com.penpals.users.penpal;

import com.penpals.common.exceptions.NotFoundException;
import com.penpals.users.AppUserService;
import com.penpals.users.RoleEnum;

import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.dto.CreatePenpalRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PenpalService {

	private final PenpalRepository penpalRepository;
	private final AppUserService appUserService;

	///////////////////////////////////////////////////////////////
	// CREATE

	public Penpal createPenpal (CreatePenpalRequest req) {
		Penpal p = new Penpal();
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		p.setRole(RoleEnum.PENPAL);
		return setOrCreateParentHelper(
			req,
			p
		);
	}

	public Penpal createPenpalForGuardian(CreatePenpalRequest req, Long guardianId) {
		// A parent/helper may only create penpals under themselves: no assigning
		// another guardian, no minting a new one (that's a monitor/admin action).
		if (req.parentHelper() != null || (req.parentHelperId() != null && !req.parentHelperId().equals(guardianId))) {
			throw new AccessDeniedException("You can only create penpals under yourself");
		}

		Penpal p = new Penpal();
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		p.setRole(RoleEnum.PENPAL);
		p.setParentHelper(appUserService.findByIdWithRole(guardianId, RoleEnum.PARENT_HELPER));

		return penpalRepository.save(p);
	}

	///////////////////////////////////////////////////////////////
	// READ

	public Penpal findById (Long id) {
		return penpalRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("No penpal with id " + id));
	}

	public List<Penpal> findAllForGuardian(Long guardianId) {
		return penpalRepository.findAllPenpalsByParentHelperId(guardianId);
	}

	public Penpal findByIdForGuardian(Long penpalId, Long guardianId) {
		return penpalRepository.findByIdAndParentHelperId(penpalId, guardianId)
			.orElseThrow(() -> new AccessDeniedException("Penpal " + penpalId + " is not yours"));
	}

	public Penpal findByIdForCompanionGuardian(Long penpalId, Long guardianId) {
		return penpalRepository.findActiveCompanionForGuardian(penpalId, guardianId)
			.orElseThrow(() -> new AccessDeniedException("No active companion for penpal " + penpalId + " (or not yours)"));
	}

	///
	/// Relationship views
	///

	// MAP 1 — every guardian once (most penpals first); each penpal + companion shown
	// exactly once. A guardian whose penpals are all already drawn (as companions) is dropped.
	public MonitorMapRelationshipView monitorChatMap() {
		List<GuardianMapRelationshipView> full =
			appUserService.findAllByRole(RoleEnum.PARENT_HELPER).stream()
				.map(g -> guardianChatMap(g.getId()))
				.sorted(Comparator.comparingInt(
					(GuardianMapRelationshipView v) -> v.penpals().size()).reversed())
				.toList();

		Set<Long> shown = new HashSet<>();
		List<GuardianMapRelationshipView> map = new ArrayList<>();

		for (GuardianMapRelationshipView gm : full) {
			List<PenpalWithCompanion> rows = new ArrayList<>();
			for (PenpalWithCompanion pc : gm.penpals()) {
				if (shown.contains(pc.penpal().id())) continue;    // already drawn as a companion
				shown.add(pc.penpal().id());
				if (pc.companion() != null) shown.add(pc.companion().id());
				rows.add(pc);
			}
			if (!rows.isEmpty()) {
				map.add(new GuardianMapRelationshipView(gm.guardian(), rows));
			} else if (gm.penpals().isEmpty()) {
				// truly childless guardian — show as a lone node (not one whose
				// penpals were all already drawn as companions elsewhere)
				map.add(new GuardianMapRelationshipView(gm.guardian(), List.of()));
			}
		}
		return new MonitorMapRelationshipView(map);
	}

	// MAP 2 — parent/helper: their penpals (admin) + each companion (bio), guardian once at the top
	public GuardianMapRelationshipView guardianChatMap (Long guardianId) {
		UserFullView guardian = UserFullView.of(appUserService.findById(guardianId));

		List<PenpalWithCompanion> penpals =
			penpalRepository.findAllPenpalsByParentHelperId(guardianId).stream()
				.map(p -> {
					Penpal comp = penpalRepository.findActiveChatCompanion(p.getId()).orElse(null);
					return new PenpalWithCompanion (
						PenpalMonitorView.of(p),
						comp == null ? null : PenpalMonitorView.of(comp));
				})
				.toList();

		return new GuardianMapRelationshipView(guardian, penpals);
	}

	// MAP 3 — penpal: self + companion, both bio
	public PenpalMapRelationshipView penpalChatMap (Long penpalId) {
		Penpal self = findById(penpalId);
		Penpal comp = penpalRepository.findActiveChatCompanion(penpalId).orElse(null);

		return new PenpalMapRelationshipView (
			PenpalBioView.of(self),
			comp == null ? null : PenpalBioView.of(comp));
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	public void updatePenpalForGuardian(Long penpalId, CreatePenpalRequest req, Long guardianId) {
		if (req.parentHelperId() != null || req.parentHelper() != null) {
			throw new AccessDeniedException("Parent helpers cannot reassign penpals.");
		}

		Penpal p = findByIdForGuardian(penpalId, guardianId);
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		penpalRepository.save(p);
	}

	public void reassignPenpal(Long penpalId, CreatePenpalRequest req) {

		Penpal p = findById(penpalId);

		setOrCreateParentHelper(req, p);
	}

	public void updatePenpalForMonitor(Long penpalId, CreatePenpalRequest req) {

		Penpal p = findById(penpalId);
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		setOrCreateParentHelper(req, p);
	}

	// HELPER

	@NonNull
	private Penpal setOrCreateParentHelper(CreatePenpalRequest req, Penpal p) {
		if (req.parentHelperId() != null) {
			p.setParentHelper(appUserService.findByIdWithRole(req.parentHelperId(), RoleEnum.PARENT_HELPER));
		} else if (req.parentHelper() != null) {
			p.setParentHelper(appUserService.createParentHelper(req.parentHelper()));
		} else {
			throw new IllegalArgumentException("A penpal must have a parent / helper");
		}
		return penpalRepository.save(p);
	}

}