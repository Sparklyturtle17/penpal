package com.penpals.users;

import com.penpals.users.dto.CreateAppUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

	private final AppUserRepository appUserRepository;

	public List<AppUser> findAllOrderedByRole() {
		return appUserRepository.findAllByOrderByRoleAsc();
	}

	public AppUser findById(Long id) {
		return appUserRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("No user with id " + id));
	}

	public AppUser findByIdWithRole(Long id, RoleEnum expectedRole) {
		AppUser user = findById(id);
		if (user.getRole() != expectedRole) {
			throw new IllegalArgumentException("User " + id + " is not a " + expectedRole);
		}
		return user;
	}

	public List<AppUser> findByIds(List<Long> ids) {
		return appUserRepository.findAllById(ids);
	}

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

	public void delete(Long id) {
		appUserRepository.deleteById(id);
	}

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