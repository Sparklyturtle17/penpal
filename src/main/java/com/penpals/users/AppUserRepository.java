package com.penpals.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	///////////////////////////////////////////////////////////////
	// CREATE

	///////////////////////////////////////////////////////////////
	// READ

	List<AppUser> findAllByOrderByRoleAsc();

	// returning users
	Optional<AppUser> findByAuthId(String authId);

	// admin, monitor, parent
	Optional<AppUser> findByEmail(String email);

	// helper
	Optional<AppUser> findByWhatsapp(String whatsapp);

	///////////////////////////////////////////////////////////////
	// UPDATE

}