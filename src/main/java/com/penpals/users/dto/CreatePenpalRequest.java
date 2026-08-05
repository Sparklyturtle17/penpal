package com.penpals.users.dto;

import com.penpals.common.State;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CreatePenpalRequest(
	@NotBlank String firstName,
	@NotBlank String lastName,
	@NotNull @Min(1) @Max(120) Integer age,
	@NotNull State state,
	@Size(max = 2000) String biography,
	Long parentHelperId,
	@Valid CreateAppUserRequest parentHelper     // <-- @Valid cascades the nested rules
) {
	// at most one guardian source: none (added server-side from the current user) or one, never both
	@AssertTrue(message = "Provide at most one of parentHelperId or parentHelper, not both")
	private boolean hasAtMostOneGuardian() {
		return !(parentHelperId != null && parentHelper != null);
	}
}