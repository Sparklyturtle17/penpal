package com.penpals.users;

import org.springframework.security.core.GrantedAuthority;

public enum RoleEnum implements GrantedAuthority {
	PENPAL, PARENT_HELPER, MONITOR, ADMIN;

	@Override
	public String getAuthority() {
		return "ROLE_" + name();
	}
}