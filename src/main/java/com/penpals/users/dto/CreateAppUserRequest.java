package com.penpals.users.dto;

import com.penpals.common.util.ValidationPatterns;
import jakarta.validation.constraints.*;

public record CreateAppUserRequest(
	@NotBlank String firstName,
	@NotBlank String lastName,
	@Email String email,                         // rule 3: if present, must be a valid email
	String phone,
	@Pattern(
		regexp = ValidationPatterns.PHONE_E164,
		message = "whatsapp must be a valid international number with country code, e.g. +14155552671"
	)
	String whatsapp
) {
	// rule 1: must have at least one contact method
	@AssertTrue(message = "Provide either an email or a whatsapp number")
	private boolean hasEmailOrWhatsapp() {
		return (email != null && !email.isBlank())
			|| (whatsapp != null && !whatsapp.isBlank());
	}
}