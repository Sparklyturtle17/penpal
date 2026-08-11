package com.penpals.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateChatRequest(
	@NotNull
	@Size(min = 2, max = 2) List<@NotNull Long> memberIds,
	Boolean active
) {}
