package com.penpals.chat.dto;

import com.penpals.chat.message.Message;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.chat.dto.ChatViews.*;

import java.time.Instant;

public interface MessageViews {

	record MessageSimpleView(
		Long id,
		String text,
		PenpalBioView penpal,
		Instant createTime
	) {
		public static MessageSimpleView of(Message m) {
			PenpalBioView author = null;
			if (m.getPenpalAuthor() == null) {
				author = new PenpalBioView(null, m.getPerformedBy().getFirstName(), null, null, null);
			} else {
				author = PenpalBioView.of(m.getPenpalAuthor());
			}

			return new MessageSimpleView(
				m.getId(),
				m.getText(),
				author,
				m.getCreateTime()
			);
		}
	}

	record MessageMonitorView(
		Long id,
		String text,
		PenpalMonitorView penpalAuthor,
		Instant createTime,
		ChatMonitorView chat,
		Boolean approved,
		Instant approvedTime
	) {
		public static MessageMonitorView of(Message m) {
			PenpalMonitorView author = null;
			if (m.getPenpalAuthor() == null) {
				author = new PenpalMonitorView(m.getPerformedBy().getId(), m.getPerformedBy().getFirstName(), m.getPerformedBy().getLastName(), null, null, "~ a monitor", null);
			} else {
				author = PenpalMonitorView.of(m.getPenpalAuthor());
			}

			return new MessageMonitorView(
				m.getId(),
				m.getText(),
				author,
				m.getCreateTime(),
				m.getChat() == null ? null : ChatMonitorView.of(m.getChat()),
				m.getApproved(),
				m.getApprovedTime()
			);
		}
	}
}
