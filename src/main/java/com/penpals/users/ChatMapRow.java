package com.penpals.users;

public record ChatMapRow(
	Long parentHelperId, String parentHelperName,
	Long penpalId, String penpalName,
	Long companionId, String companionName,
	Long companionParentHelperId, String companionParentHelperName
) {}