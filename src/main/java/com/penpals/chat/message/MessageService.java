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
import java.util.List;

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

		return messageRepository.save(m);
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

	public Message updateMessageTextForMonitor(UpdateMessageTextOnlyRequest request, Long messageId, Long performedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		AppUser editor = appUserRepository.findById(performedById)
			.orElseThrow(() -> new NotFoundException("No user " + performedById));

		// TODO AUDIT STUFF
		//		AuditMessage audit = new AuditMessage();
		//		audit.set
		//		Long id = auditRepository.save(audit);

		//		if (id == null) {
		//			throw new InternalException("Could not save, audit failed " + req);
		//		}

		m.setText(request.text());
		m.setPerformedBy(editor);

		return messageRepository.save(m);
	}

	public Message approveMessage(ApprovalMessageRequest req, Long messageId, Long approvedById) {
		Message m = messageRepository.findById(messageId)
			.orElseThrow(() -> new NotFoundException("No message " + messageId));
		AppUser approvedBy = appUserRepository.findById(approvedById)
			.orElseThrow(() -> new NotFoundException("No user " + approvedById));

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

		return messageRepository.save(m);
	}
}
