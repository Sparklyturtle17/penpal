package com.penpals.users.penpal;

import com.penpals.users.AppUserService;
import com.penpals.users.RoleEnum;

import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.dto.CreatePenpalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PenpalService {

	private final PenpalRepository penpalRepository;
	private final AppUserService appUserService;

	public Penpal findById (Long id) {
		return penpalRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("No user with id " + id));
	}

	public Penpal createPenpal (CreatePenpalRequest req) {
		Penpal p = new Penpal();
		p.setFirstName(req.firstName());
		p.setLastName(req.lastName());
		p.setAge(req.age());
		p.setState(req.state());
		p.setBiography(req.biography());
		p.setRole(RoleEnum.PENPAL);
		if (req.parentHelperId() != null) {
			p.setParentHelper(appUserService.findByIdWithRole(req.parentHelperId(), RoleEnum.PARENT_HELPER));
		} else if (req.parentHelper() != null) {
			p.setParentHelper(appUserService.createParentHelper(req.parentHelper()));
		} else {
			throw new IllegalArgumentException("A penpal must have a parent / helper");
		}

		return penpalRepository.save(p);
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
}