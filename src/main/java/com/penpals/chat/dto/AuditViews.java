package com.penpals.chat.dto;

import com.penpals.chat.message.audit.MessageAudit;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.chat.dto.MessageViews.*;
import com.penpals.chat.dto.ChatViews.*;
import com.penpals.users.dto.PenpalViews.*;

import java.time.Instant;
import java.util.List;

public interface AuditViews {

	record ListOfAudits(
		List<AuditFullView> auditFullViewList
	) {
		public static ListOfAudits of(List<MessageAudit> messageAuditList) {
			return new ListOfAudits(
				messageAuditList.stream().map(AuditFullView::of).toList()
			);
		}
	}

	record AuditFullView(
		Long auditId,
		Instant archiveTime,
		UserFullView editedBy,
		MessageMonitorView currentMessageState,
		String text,
		PenpalMonitorView penpalAuthor,
		UserFullView performedBy,
		Instant createTime,
		ChatMonitorView chat,
		Boolean approved,
		UserFullView approvedBy,
		Instant approvedTime
	) {
		private static AuditFullView of(MessageAudit messageAudit) {

			return new AuditFullView(
				messageAudit.getAuditId(),
				messageAudit.getArchiveTime(),
				messageAudit.getEditedBy() != null ? UserFullView.of(messageAudit.getEditedBy()) : null,
				MessageMonitorView.of(messageAudit.getMessage()),
				messageAudit.getText(),
				messageAudit.getPenpalAuthor() != null
					? PenpalMonitorView.of(messageAudit.getPenpalAuthor())
					: new PenpalMonitorView(null, messageAudit.getPerformedBy().getFirstName(), messageAudit.getPerformedBy().getLastName(), null, null, "~ a monitor", null),
				UserFullView.of(messageAudit.getPerformedBy()),
				messageAudit.getCreateTime(),
				ChatMonitorView.of(messageAudit.getChat()),
				messageAudit.getApproved(),
				messageAudit.getApprovedBy() != null ? UserFullView.of(messageAudit.getApprovedBy()) : null,
				messageAudit.getApprovedTime()
			);
		}
	}
}
