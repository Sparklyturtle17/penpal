package com.penpals.chat.message;

import com.penpals.chat.Chat;
import com.penpals.chat.ChatRepository;
import com.penpals.chat.dto.MessageRequests.*;
import com.penpals.common.exceptions.NotFoundException;
import com.penpals.users.AppUser;
import com.penpals.users.AppUserRepository;
import com.penpals.users.RoleEnum;
import com.penpals.users.penpal.Penpal;
import com.penpals.users.penpal.PenpalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final PenpalRepository penpalRepository;
	private final ChatRepository chatRepository;
	private final AppUserRepository appUserRepository;

	///////////////////////////////////////////////////////////////
	// CREATE

	public Message createMessage(CreateNewMessageRequest req, Long penpalId, Long performedById) {
		Penpal author = penpalRepository.findById(penpalId)
			.orElseThrow(() -> new NotFoundException("No penpal " + penpalId));
		Chat chat = chatRepository.findById(req.chatId())
			.orElseThrow(() -> new NotFoundException("No chat " + req.chatId()));
		AppUser performedBy = appUserRepository.findById(performedById)
			.orElseThrow(() -> new NotFoundException("No user " + performedById));

		if (!chat.getMembers().contains(author)) {
			throw new IllegalArgumentException("Author is not in this chat");
		}
		if (!author.getParentHelper().equals(performedBy) || !performedBy.getRole().equals(RoleEnum.PARENT_HELPER)) {
			throw new IllegalArgumentException("Performed by is not a parent/ helper of author.");
		}
		if (!chat.getActive()) {
			throw new IllegalArgumentException("Chat is not active.");
		}

		Message m = new Message();
		m.setText(req.text());
		m.setPenpalAuthor(author);
		m.setPerformedBy(performedBy);
		m.setCreateTime(Instant.now());
		m.setChat(chat);
		m.setApproved(false);

		return messageRepository.save(m);
	}

	///////////////////////////////////////////////////////////////
	// READ

	public Message findMessagesByIdInMyChat (Long id, Long penpalId) {
		return messageRepository.findByIdAndEligiblePenpals(id, penpalId)
			.orElseThrow(() -> new NotFoundException("No message " + id));
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	public Message updateMessageText(UpdateMessageTextOnlyRequest request, Long messageId, Long penpalId, Long performedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		Penpal penpalAuthor = penpalRepository.findById(penpalId)
			.orElseThrow(() -> new NotFoundException("No user " + penpalId));
		AppUser performedBy = appUserRepository.findById(performedById)
			.orElseThrow(() -> new NotFoundException("No user " + performedById));

		if (!m.getPenpalAuthor().equals(penpalAuthor) && !(Set.of(RoleEnum.ADMIN, RoleEnum.MONITOR).contains(m.getPenpalAuthor().getRole()))) {
			throw new AccessDeniedException("You are not allowed to perform this action");
		}

		// TODO AUDIT STUFF
//		AuditMessage audit = new AuditMessage();
//		audit.set
//		Long id = auditRepository.save(audit);

//		if (id == null) {
//			throw new InternalException("Could not save, audit failed " + req);
//		}

		m.setText(request.text());
		m.setPerformedBy(performedBy);

		return messageRepository.save(m);
	}

	public Message approveMessage(ApprovalMessageRequest req, Long approvedById) {
		Message m = messageRepository.findById(req.messageId())
			.orElseThrow(() -> new NotFoundException("No message " + req.messageId()));
		AppUser approvedBy = appUserRepository.findById(approvedById)
			.orElseThrow(() -> new NotFoundException("No user " + approvedById));

		if (!Set.of(RoleEnum.MONITOR, RoleEnum.ADMIN).contains(approvedBy.getRole())) {
			throw new AccessDeniedException("You are not allowed to approve or unapprove this message.");
		}

		// TODO AUDIT STUFF
		//		AuditMessage audit = new AuditMessage();
		//		audit.set
		//		Long id = auditRepository.save(audit);

		//		if (id == null) {
		//			throw new InternalException("Could not save, audit failed " + req);
		//		}

		m.setApproved(req.approved());
		m.setApprovedBy(approvedBy);
		m.setApprovedTime(Instant.now());

		return messageRepository.save(m);
	}
}
