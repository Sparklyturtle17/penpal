package com.penpals.chat;

import com.penpals.chat.dto.CreateChatRequest;
import com.penpals.chat.message.Message;
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

	public Chat findMyChatById (Long id, Long penpalId) {
		return chatRepository.findByIdAndEligiblePenpals(id, penpalId)
			.orElseThrow(() -> new NotFoundException("No message " + id));
	}

	///////////////////////////////////////////////////////////////
	// UPDATE
}