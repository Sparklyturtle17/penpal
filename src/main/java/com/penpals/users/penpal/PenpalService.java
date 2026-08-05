package com.penpals.users.penpal;

import com.penpals.users.AppUserService;
import com.penpals.users.ChatMapRow;
import com.penpals.users.RoleEnum;

import com.penpals.users.dto.ChatMapView.*;
import com.penpals.users.dto.CreatePenpalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class PenpalService {

	private final PenpalRepository penpalRepository;
	private final AppUserService appUserService;

	public Penpal findById(Long id) {
		return penpalRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("No user with id " + id));
	}

	public Penpal createPenpal(CreatePenpalRequest req) {
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

	public void delete(Long id) {
		penpalRepository.deleteById(id);
	}

	public List<GuardianNode> fullChatMapTree() {
		Map<Long, String> guardianName = new LinkedHashMap<>();
		Map<Long, List<PenpalNode>> penpalsByGuardian = new LinkedHashMap<>();

		for (ChatMapRow r : penpalRepository.findCompleteChatMap()) {
			guardianName.putIfAbsent(r.parentHelperId(), r.parentHelperName());
			penpalsByGuardian
				.computeIfAbsent(r.parentHelperId(), k -> new ArrayList<>())
				.add(new PenpalNode(
					r.penpalId(), r.penpalName(),
					new Companion(
						r.companionId(), r.companionName(),
						r.companionParentHelperId(), r.companionParentHelperName())));
		}

		return penpalsByGuardian.entrySet().stream()
			.map(e -> new GuardianNode(
				e.getKey(), guardianName.get(e.getKey()), e.getValue()))
			.toList();
	}

	public GuardianNode chatMapForGuardian(Long parentHelperId) {
		List<PenpalNode> penpals = new ArrayList<>();
		String guardianName = null;

		for (ChatMapRow r : penpalRepository.findChatMapByParentHelper(parentHelperId)) {
			guardianName = r.parentHelperName();               // same guardian on every row
			penpals.add(new PenpalNode(
				r.penpalId(),
				r.penpalName(),
				new Companion(
					r.companionId(),
					r.companionName(),
					null,
					null
				)
			));                              // <-- no companion guardian (g–p–p)
		}
		return new GuardianNode(parentHelperId, guardianName, penpals);
	}
}