package com.penpals.chat.message;

import com.penpals.chat.Chat;
import com.penpals.chat.ChatRepository;
import com.penpals.chat.dto.MessageRequests.*;
import com.penpals.chat.message.audit.MessageAudit;
import com.penpals.chat.message.audit.MessageAuditService;
import com.penpals.common.exceptions.NotFoundException;
import com.penpals.users.AppUser;
import com.penpals.users.AppUserRepository;
import com.penpals.users.RoleEnum;
import com.penpals.users.penpal.Penpal;
import com.penpals.users.penpal.PenpalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

	private final MessageRepository messageRepository;
	private final PenpalRepository penpalRepository;
	private final ChatRepository chatRepository;
	private final AppUserRepository appUserRepository;
	private final MessageAuditService messageAuditService;

	///////////////////////////////////////////////////////////////
	// CREATE

	@Transactional
	public Message createMessage(CreateNewMessageRequest req, Long penpalId, Long performedById) {
		Penpal author = penpalRepository.findById(penpalId)
			.orElseThrow(() -> new NotFoundException("No penpal " + penpalId));
		Chat chat = chatRepository.findById(req.chatId())
			.orElseThrow(() -> new NotFoundException("No chat " + req.chatId()));
		AppUser performedBy = appUserRepository.findById(performedById)
			.orElseThrow(() -> new NotFoundException("No user " + performedById));

		if (author != null && !chat.getMembers().contains(author)) {
			throw new AccessDeniedException("Author is not in this chat");
		}
		if (!author.getParentHelper().equals(performedBy) || !performedBy.getRole().equals(RoleEnum.PARENT_HELPER)) {
			throw new AccessDeniedException("Performed by is not a parent/ helper of author.");
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
		m.setApproved(null);   // pending until a monitor approves (null = awaiting review)

		Message created = messageRepository.save(m);

		messageAuditService.createAuditRecord(created, null);
		// if this throws an exception, because transactional everything will roll back

		return created;
	}

	public List<Message> broadcastToAllChats(CreateBlastMessageRequest request, Long monitorId) {

		AppUser monitor = appUserRepository.findById(monitorId)
			.orElseThrow(() -> new NotFoundException("No user " + monitorId));

		return chatRepository.findAllByActiveTrue().stream()
			.map(chat -> {
				Message m = new Message();
				m.setText(request.text());
				m.setPenpalAuthor(null);
				m.setPerformedBy(monitor);
				m.setChat(chat);
				m.setCreateTime(Instant.now());
				m.setApproved(true);
				return m;
			})
			.map(messageRepository::save)
			.map((message) -> {
				messageAuditService.createAuditRecord(message, monitor);
				return message;
			})
			.toList();
	}

	///////////////////////////////////////////////////////////////
	// READ

	public Message findMessagesByIdInMyChat (Long id, Long penpalId) {
		return messageRepository.findByIdAndEligiblePenpals(id, penpalId)
			.orElseThrow(() -> new NotFoundException("No message " + id));
	}

	public Message findById (Long id) {
		return messageRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("No message " + id));
	}

	public List<Message> findAll () {
		return messageRepository.findAll();
	}

	public List<Message> findAllUnreviewed () {
		return messageRepository.findAllByApprovedFalseOrApprovedNull();
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@Transactional
	public Message updateMessageTextForPenpal(UpdateMessageTextOnlyRequest request, Long messageId, Long penpalAuthorId, Long performedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		Penpal author = penpalRepository.findById(penpalAuthorId)
			.orElseThrow(() -> new NotFoundException("No penpal " + penpalAuthorId));
		AppUser performedBy = appUserRepository.findById(performedById)
			.orElseThrow(() -> new NotFoundException("No user " + performedById));

		// a penpal (acting via their guardian) may only edit their OWN message
		if (!author.equals(m.getPenpalAuthor())) {
			throw new AccessDeniedException("You can only edit your own message");
		}

		m.setText(request.text());
		m.setPerformedBy(performedBy);

		messageAuditService.createAuditRecord(m, performedBy);
		// if this throws an exception, because transactional everything will roll back

		return messageRepository.save(m);
	}

	@Transactional
	public Message updateMessageTextForMonitor(UpdateMessageTextOnlyRequest request, Long messageId, Long performedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		AppUser editor = appUserRepository.findById(performedById)
			.orElseThrow(() -> new NotFoundException("No user " + performedById));

		m.setText(request.text());
		m.setPerformedBy(editor);

		messageAuditService.createAuditRecord(m, editor);
		// if this throws an exception, because transactional everything will roll back

		return messageRepository.save(m);
	}

	@Transactional
	public Message approveMessage(ApprovalMessageRequest req, Long messageId, Long approvedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		AppUser approvedBy = appUserRepository.findById(approvedById)
			.orElseThrow(() -> new NotFoundException("No user " + approvedById));

		m.setApproved(req.approved());
		m.setApprovedBy(approvedBy);
		m.setApprovedTime(Instant.now());

		messageAuditService.createAuditRecord(m, approvedBy);
		// if this throws an exception, because transactional everything will roll back

		return messageRepository.save(m);
	}

	@Transactional
	public Message fakeRemoveMessage(Long messageId, Long chatId, Long approvedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		Chat c = chatRepository.findById(chatId)
			.orElseThrow(() -> new NotFoundException("No chat " + chatId));
		AppUser approvedBy = appUserRepository.findById(approvedById)
			.orElseThrow(() -> new NotFoundException("No user " + approvedById));

		if (!m.getChat().getId().equals(chatId)) {
			throw new IllegalArgumentException("That message is not in this chat");
		}

		m.setPerformedBy(approvedBy);
		m.setChat(null);

		messageAuditService.createAuditRecord(m, approvedBy);
		// if this throws an exception, because transactional everything will roll back

		return messageRepository.save(m);
	}
}
