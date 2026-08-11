package com.penpals.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface MessageRequests {

	record CreateNewMessageRequest(
		@NotBlank String text,
		@NotNull Long chatId
	) {}

	record UpdateMessageTextOnlyRequest(
		@NotBlank String text
	) {}

	record ApprovalMessageRequest(
		@NotNull Boolean approved
	) {}

	record CreateBlastMessageRequest(
		@NotBlank String text
	) {}

}
