package com.penpals.access.parenthelper;

import com.penpals.access.CurrentUserService;
import com.penpals.chat.ChatService;
import com.penpals.chat.dto.ChatMessagesView.*;
import com.penpals.chat.dto.ChatViews.*;
import com.penpals.chat.dto.MessageRequests;
import com.penpals.chat.dto.MessageRequests.*;
import com.penpals.chat.dto.MessageViews.*;
import com.penpals.chat.message.Message;
import com.penpals.chat.message.MessageService;
import com.penpals.common.ApiResponses;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.PenpalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penpal/penpals")
@PreAuthorize("hasAnyRole('PENPAL')")
@Slf4j
@RequiredArgsConstructor
public class PenpalController {

	private final PenpalService penpalService;
	private final MessageService messageService;
	private final ChatService chatService;
	private final CurrentUserService currentUserService;

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	///////////////////////////////////////////////////////////////
	// READ

	@GetMapping("/me")
	public PenpalBioView me(
		@RequestAttribute(name = ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		if (penpalId == null) {
			throw new AccessDeniedException("App users other than penpal must use the default endpoint for /me.");
		}
		return PenpalBioView.of(penpalService.findById(penpalId));
	}

	@GetMapping("/relations")
	public PenpalMapRelationshipView relations (@RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		return penpalService.penpalChatMap(penpalId);
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@PostMapping("/messages")
	public ResponseEntity<Void> create(
		@Valid @RequestBody CreateNewMessageRequest request,
		@RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		Message created = messageService.createMessage(request, penpalId, currentUserService.currentId());
		return ApiResponses.created(created.getId());
	}

	///////////////////////////////////////////////////////////////
	// READ

	@GetMapping("/messages/{id}")
	public MessageSimpleView viewMessage(@PathVariable Long id, @RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		return MessageSimpleView.of(messageService.findMessagesByIdInMyChat(id, penpalId));
	}

	@GetMapping("/chats/{id}")
	public SimpleChatMessageView viewChat(@PathVariable Long id, @RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		return chatService.findMyChatById(id, penpalId);
	}

	@GetMapping("/chats")
	public List<ChatSimpleView> myChats(@RequestAttribute(ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId) {
		return chatService.findAllForPenpal(penpalId).stream().map(ChatSimpleView::of).toList();
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@PutMapping("/messages/{messageId}")
	public ResponseEntity<Void> updateMessageText(@Valid @RequestBody MessageRequests.UpdateMessageTextOnlyRequest request, @RequestAttribute(name = ActingAsPenpalFilter.ACTIVE_PENPAL_ATTR) Long penpalId, @PathVariable Long messageId) {
		messageService.updateMessageTextForPenpal(request, messageId, penpalId, currentUserService.currentId());
		return ResponseEntity.noContent().build();
	}
}
