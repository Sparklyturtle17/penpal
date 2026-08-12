package com.penpals.chat;

import com.penpals.chat.dto.ChatMessagesView.*;
import com.penpals.chat.dto.CreateChatRequest;
import com.penpals.chat.message.Message;
import com.penpals.chat.message.MessageRepository;
import com.penpals.common.exceptions.NotFoundException;
import com.penpals.users.penpal.Penpal;
import com.penpals.users.penpal.PenpalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRepository chatRepository;
	private final PenpalRepository penpalRepository;
	private final MessageRepository messageRepository;

	///////////////////////////////////////////////////////////////
	// CREATE

	public Chat createChat(CreateChatRequest req) {
		List<Long> ids = req.memberIds();

		if (ids.get(0).equals(ids.get(1))) {
			throw new IllegalArgumentException("A chat needs two different penpals");
		}

		List<Penpal> members = penpalRepository.findAllById(ids);
		if (members.size() != ids.size()) {
			throw new NotFoundException("One or more penpal ids do not exist");
		}

		Chat c = new Chat();
		c.setMembers(members);
		c.setActive(req.active() == null || req.active());

		return chatRepository.save(c);
	}

	///////////////////////////////////////////////////////////////
	// READ

	public SimpleChatMessageView findMyChatById (Long id, Long penpalId) {
		Chat chat = chatRepository.findByIdAndEligiblePenpals(id, penpalId)
			.orElseThrow(() -> new NotFoundException("No message " + id));
		// hide the companion's unapproved messages; keep all of the penpal's own
		List<Message> messages = messageRepository.findVisibleInChatForPenpal(chat.getId(), penpalId);
		return SimpleChatMessageView.of(chat, messages);
	}

	public List<Chat> findAllForPenpal(Long penpalId) {
		return chatRepository.findAllByMemberId(penpalId);
	}

	public Chat findById (Long id) {
		return chatRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("No message " + id));
	}

	public List<MonitorChatMessageView> findAll () {
		return chatRepository.findAll().stream()
			.map(c -> MonitorChatMessageView.of(c, messageRepository.findAllByChatId(c.getId())))
			.toList();
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	public Chat updateChatActivation (Long id, boolean active) {
		Chat chat = findById(id);
		chat.setActive(active);
		return chatRepository.save(chat);
	}

}