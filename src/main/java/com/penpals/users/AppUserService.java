package com.penpals.users;

import com.penpals.common.exceptions.NotFoundException;
import com.penpals.users.dto.CreateAppUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

	private final AppUserRepository appUserRepository;

	///////////////////////////////////////////////////////////////
	// CREATE

	public AppUser createParentHelper(CreateAppUserRequest req) {
		AppUser parentHelper = new AppUser();
		parentHelper.setFirstName(req.firstName());
		parentHelper.setLastName(req.lastName());
		parentHelper.setEmail(req.email());
		parentHelper.setPhone(req.phone());
		parentHelper.setWhatsapp(req.whatsapp());
		parentHelper.setRole(RoleEnum.PARENT_HELPER);
		return appUserRepository.save(parentHelper);
	}

	public AppUser createMonitor(CreateAppUserRequest req) {
		AppUser monitor = new AppUser();
		monitor.setFirstName(req.firstName());
		monitor.setLastName(req.lastName());
		monitor.setEmail(req.email());
		monitor.setPhone(req.phone());
		monitor.setWhatsapp(req.whatsapp());
		monitor.setRole(RoleEnum.MONITOR);
		return appUserRepository.save(monitor);
	}

	///////////////////////////////////////////////////////////////
	// READ

	public List<AppUser> findAllOrderedByRole() {
		return appUserRepository.findAllByOrderByRoleAsc();
	}


	public List<AppUser> findAllByRole(RoleEnum roleEnum) {
		return appUserRepository.findAllByRole(roleEnum);
	}

	public AppUser findById(Long id) {
		return appUserRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("No user with id " + id));
	}

	public AppUser findByIdWithRole(Long id, RoleEnum expectedRole) {
		AppUser user = findById(id);
		if (user.getRole() != expectedRole) {
			throw new IllegalArgumentException("User " + id + " is not a " + expectedRole);
		}
		return user;
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	public AppUser update(Long id, CreateAppUserRequest body) {
		AppUser user = findById(id);
		user.setFirstName(body.firstName());
		user.setLastName(body.lastName());
		user.setEmail(body.email());
		user.setPhone(body.phone());
		user.setWhatsapp(body.whatsapp());
		return appUserRepository.save(user);
	}
}