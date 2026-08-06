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

import java.util.List;

@Service
@RequiredArgsConstructor
public class PenpalService {

	private final PenpalRepository penpalRepository;
	private final AppUserService appUserService;

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

	public Penpal findById (Long id) {
		return penpalRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("No penpal with id " + id));
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

	// MAP 1 — monitor: every active chat, both penpals in admin view
	public List<MonitorMapRelationshipView> monitorChatMap () {
		return penpalRepository.findActiveChatPairs().stream()
			.map(pair -> new MonitorMapRelationshipView (
				PenpalAdminView.of((Penpal) pair[0]),
				PenpalAdminView.of((Penpal) pair[1])))
			.toList();
	}

	// MAP 2 — parent/helper: their penpals (admin) + each companion (bio), guardian once at the top
	public GuardianMapRelationshipView guardianChatMap (Long guardianId) {
		UserFullView guardian = UserFullView.of(appUserService.findById(guardianId));

		List<PenpalWithCompanion> penpals =
			penpalRepository.findAllPenpalsByParentHelperId(guardianId).stream()
				.map(p -> {
					Penpal comp = penpalRepository.findActiveChatCompanion(p.getId()).orElse(null);
					return new PenpalWithCompanion (
						PenpalAdminView.of(p),
						comp == null ? null : PenpalBioView.of(comp));
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

	///
	///
	///

	public Penpal updatePenpalForGuardian(Long penpalId, CreatePenpalRequest req, Long guardianId) {
		if (req.parentHelperId() != null || req.parentHelper() != null) {
			throw new AccessDeniedException("Parent helpers cannot reassign penpals.");
		}

		Penpal p = findByIdForGuardian(penpalId, guardianId);
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		return penpalRepository.save(p);
	}

	public Penpal reassignPenpal(Long penpalId, CreatePenpalRequest req) {

		Penpal p = findById(penpalId);

		return setOrCreateParentHelper(req, p);
	}

	public Penpal updatePenpalForMonitor(Long penpalId, CreatePenpalRequest req) {

		Penpal p = findById(penpalId);
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		return setOrCreateParentHelper(req, p);
	}

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