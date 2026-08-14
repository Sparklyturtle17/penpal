package com.penpals.access.monitor;

import com.penpals.access.CurrentUserService;
import com.penpals.chat.Chat;
import com.penpals.chat.ChatService;
import com.penpals.chat.dto.ChatMessagesView.*;
import com.penpals.chat.dto.ChatViews.*;
import com.penpals.chat.dto.CreateChatRequest;
import com.penpals.chat.dto.MessageRequests.*;
import com.penpals.chat.dto.MessageViews.*;
import com.penpals.chat.message.Message;
import com.penpals.chat.message.MessageService;
import com.penpals.common.ApiResponses;
import com.penpals.users.AppUser;
import com.penpals.users.AppUserService;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreateAppUserRequest;
import com.penpals.users.dto.RelationshipsView.*;
import com.penpals.users.penpal.Penpal;
import com.penpals.users.penpal.PenpalService;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/penpal/monitors")
@PreAuthorize("hasAnyRole('MONITOR', 'ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class MonitorController {

	private final PenpalService penpalService;
	private final AppUserService appUserService;
	private final MessageService messageService;
	private final CurrentUserService currentUserService;
	private final ChatService chatService;
	private final NaughtyWordsService naughtyWordsService;


	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@PostMapping("/penpals")
	public ResponseEntity<Void> create(@Valid @RequestBody CreatePenpalRequest body) {
		Penpal created = penpalService.createPenpal(body);
		return ApiResponses.created(created.getId());
	}

	@PostMapping("/parent-helpers")
	public ResponseEntity<Void> create(@Valid @RequestBody CreateAppUserRequest body) {
		AppUser created = appUserService.createParentHelper(body);
		return ApiResponses.created(created.getId());
	}

	///////////////////////////////////////////////////////////////
	// READ

	@GetMapping("/penpals/{id}")
	public PenpalMonitorView viewPenpal(@PathVariable Long id) {
		return PenpalMonitorView.of(penpalService.findById(id));
	}

	@GetMapping("/parent-helpers/{id}")
	public UserFullView viewParentHelper(@PathVariable Long id) {
		return UserFullView.of(appUserService.findByIdWithRole(id, RoleEnum.PARENT_HELPER));
	}

	@GetMapping("/monitors/{id}")
	public UserFullView viewMonitor(@PathVariable Long id) {
		return UserFullView.of(appUserService.findByIdWithRole(id, RoleEnum.MONITOR));
	}

	@GetMapping("/admins/{id}")
	public UserFullView viewAdmin(@PathVariable Long id) {
		return UserFullView.of(appUserService.findByIdWithRole(id, RoleEnum.ADMIN));
	}

	@GetMapping("/relations")
	public MonitorMapRelationshipView monitorRelationshipMap() {
		return penpalService.monitorChatMap();
	}

	@GetMapping("/all-users")
	public List<UserFullView> allUsers() {
		return appUserService.findAllOrderedByRole().stream()
			.map(UserFullView::of)
			.toList();
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@PutMapping("/reassign-penpal/{id}")
	public ResponseEntity<Void> reassignPenpal(@PathVariable Long id, @Valid @RequestBody CreatePenpalRequest body) {
		penpalService.reassignPenpal(id, body);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/penpals/{id}")
	public ResponseEntity<Void> updatePenpal(@PathVariable Long id, @Valid @RequestBody CreatePenpalRequest body) {
		penpalService.updatePenpalForMonitor(id, body);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/parent-helpers/{id}")
	public ResponseEntity<Void> updateParentHelper(@PathVariable Long id, @Valid @RequestBody CreateAppUserRequest body) {
		appUserService.update(id, body);
		return ResponseEntity.noContent().build();
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@PostMapping("/messages")
	public ResponseEntity<Void> createBlastMessage(@Valid @RequestBody CreateBlastMessageRequest request) {
		List<Message> created = messageService.broadcastToAllChats(request, currentUserService.currentId());
		return ApiResponses.created(created.getFirst().getId());
	}

	@PostMapping("/chats")
	public ResponseEntity<Void> createChat(@Valid @RequestBody CreateChatRequest body) {
		Chat chat = chatService.createChat(body);
		return ApiResponses.created(chat.getId());
	}

	///////////////////////////////////////////////////////////////
	// READ

	@GetMapping("/naughty-words")
	public List<String> naughtyWords() {
		return naughtyWordsService.words();
	}

	@GetMapping("/messages/{id}")
	public MessageMonitorView viewMessage(@PathVariable Long id) {
		return MessageMonitorView.of(messageService.findById(id));
	}

//	@GetMapping("/messages/all")
//	public List<MessageMonitorView> viewAllMessages() {
//		return messageService.findAll().stream().map(MessageMonitorView::of).toList();
//	}

	@GetMapping("/messages/unapproved")
	public List<MessageMonitorView> viewAllUnapprovedMessages() {
		return messageService.findAllUnreviewed().stream().map(MessageMonitorView::of).toList();
	}

	@GetMapping("/chats/{id}")
	public ChatMonitorView viewChat(@PathVariable Long id) {
		return ChatMonitorView.of(chatService.findById(id));
	}

	@GetMapping("/chats/all")
	public List<MonitorChatMessageView> viewAllChats() {
		return chatService.findAll();
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@PutMapping("/messages/{id}")
	public ResponseEntity<Void> updateMessageText(@PathVariable Long id, @Valid @RequestBody UpdateMessageTextOnlyRequest request) {
		messageService.updateMessageTextForMonitor(request, id, currentUserService.currentId());
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/messages/{id}/approval")
	public ResponseEntity<Void> updateMessageApproval(@PathVariable Long id, @Valid @RequestBody ApprovalMessageRequest request) {
		messageService.approveMessage(request, id, currentUserService.currentId());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/messages/{messageId}/chats/{chatId}")
	public ResponseEntity<Void> updateChatRemoveMessage(@PathVariable Long messageId, @PathVariable Long chatId) {
		messageService.fakeRemoveMessage(messageId, chatId, currentUserService.currentId());
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/chats/{id}/activation")
	public ResponseEntity<Void> updateChatActivation(@PathVariable Long id, @RequestParam boolean active) {
		chatService.updateChatActivation(id, active);
		return ResponseEntity.noContent().build();
	}
}