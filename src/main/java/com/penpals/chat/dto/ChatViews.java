package com.penpals.chat.dto;

import com.penpals.chat.Chat;
import com.penpals.users.dto.PenpalViews.*;

import java.util.List;

public interface ChatViews {

	record ChatMonitorView(
		Long id,
		List<PenpalMonitorView> members,
		Boolean active
	) {
		public static ChatMonitorView of(Chat c) {
			List<PenpalMonitorView> members = c.getMembers().stream().map(PenpalMonitorView::of).toList();

			return new ChatMonitorView(
				c.getId(),
				members,
				c.getActive()
			);
		}
	}

	record ChatSimpleView(
		Long id,
		List<PenpalBioView> members,
		Boolean active
	) {
		public static ChatSimpleView of(Chat c) {
			List<PenpalBioView> members = c.getMembers().stream().map(PenpalBioView::of).toList();

			return new ChatSimpleView(
				c.getId(),
				members,
				c.getActive()
			);
		}
	}
}
