package com.penpals.users.dto;

import com.penpals.users.AppUser;
import com.penpals.users.RoleEnum;

public interface AppUserViews {

	record UserFullView(
		Long id,
		String firstName,
		String lastName,
		String email,
		String phone,
		String whatsapp,
		RoleEnum role
	) {
		public static UserFullView of(AppUser u) {
			return new UserFullView(
				u.getId(),
				u.getFirstName(),
				u.getLastName(),
				u.getEmail(),
				u.getPhone(),
				u.getWhatsapp(),
				u.getRole()
			);
		}
	}

	record UserSummaryView(
		Long id,
		String firstName,
		String lastName
	) {
		public static UserSummaryView of(AppUser u) {
			return new UserSummaryView(
				u.getId(),
				u.getFirstName(),
				u.getLastName()
			);
		}
	}
}
