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
	@Valid CreateParentHelperRequest parentHelper     // <-- @Valid cascades the nested rules
) {
	// exactly one guardian source: link an existing one OR create a new one
	@AssertTrue(message = "Provide exactly one of parentHelperId or parentHelper")
	private boolean hasExactlyOneGuardian() {
		return (parentHelperId != null) ^ (parentHelper != null);   // XOR
	}
}