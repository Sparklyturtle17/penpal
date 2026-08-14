package com.penpals.chat.message.audit;

import com.penpals.chat.message.Message;
import com.penpals.chat.message.MessageRepository;
import com.penpals.common.exceptions.NotFoundException;
import com.penpals.users.AppUser;
import com.penpals.users.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageAuditService {

	private final MessageAuditRepository messageAuditRepository;
	private final AppUserService appUserService;
	private final MessageRepository messageRepository;

	public MessageAudit createAuditRecord(Message message, AppUser editedBy) {

		MessageAudit messageAudit = new MessageAudit();
		messageAudit.setArchiveTime(Instant.now());
		messageAudit.setEditedBy(editedBy);
		messageAudit.setMessage(message);
		messageAudit.setText(message.getText());
		messageAudit.setPenpalAuthor(message.getPenpalAuthor());
		messageAudit.setPerformedBy(message.getPerformedBy());
		messageAudit.setCreateTime(message.getCreateTime());
		messageAudit.setChat(message.getChat());
		messageAudit.setApproved(message.getApproved());
		messageAudit.setApprovedBy(message.getApprovedBy());
		messageAudit.setApprovedTime(message.getApprovedTime());

		return messageAuditRepository.save(messageAudit);
	}

	public List<MessageAudit> getAuditRecordsByMessage() {

		return messageAuditRepository.findAllForMessageGroupedView();
	}

	public List<MessageAudit> getAuditRecordsByMessageId(Long messageId) {
		Message message = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));

		return messageAuditRepository.findAllByMessageOrderByArchiveTimeDesc(message);
	}

	public List<MessageAudit> getAuditRecordsTouchedByAppUserId(Long appUserId) {
		AppUser appUser = appUserService.findById(appUserId);

		return messageAuditRepository.findAllTouchedBy(appUser);
	}
}