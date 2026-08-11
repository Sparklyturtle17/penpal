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
			return new MessageSimpleView(
				m.getId(),
				m.getText(),
				PenpalBioView.of(m.getPenpalAuthor()),
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
			return new MessageMonitorView(
				m.getId(),
				m.getText(),
				PenpalMonitorView.of(m.getPenpalAuthor()),
				m.getCreateTime(),
				ChatMonitorView.of(m.getChat()),
				m.getApproved(),
				m.getApprovedTime()
			);
		}
	}
}
