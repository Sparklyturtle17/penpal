package com.penpals.access.monitor;

import com.penpals.ControllerTestBase;
import com.penpals.chat.dto.ChatMessagesView.*;
import com.penpals.chat.dto.ChatViews;
import com.penpals.chat.dto.ChatViews.*;
import com.penpals.chat.dto.CreateChatRequest;
import com.penpals.chat.dto.MessageRequests.*;
import com.penpals.chat.dto.MessageViews;
import com.penpals.chat.dto.MessageViews.*;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreateAppUserRequest;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.penpals.SeedData.*;
import static com.penpals.TestFixtures.Penpals.*;
import static com.penpals.TestFixtures.Users.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class MonitorControllerTest extends ControllerTestBase {

	RequestPostProcessor MONITOR_AUTH = httpBasic("monitor", "monitor");

	CreatePenpalRequest NEW_PENPAL_WITH_NULL_PARENT_HELPER = PENPAL_A;
	CreatePenpalRequest NEW_PENPAL_WITH_EXISTING_PARENT_HELPER = PENPAL_C;
	CreatePenpalRequest NEW_PENPAL_WITH_NEW_PARENT_HELPER = PENPAL_D;
	CreateAppUserRequest NEW_PARENT_HELPER = APP_USER_A;

	//╔═════════════════════════════════════════════════════════╗
	//║                           AUTH                          ║
	//╚═════════════════════════════════════════════════════════╝

	@Test
	void guardianActingAs_theirPenpal_cannotHit_monitorEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/monitors/relations")
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_cannotHit_monitorEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/monitors/relations")
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void monitor_canHit_monitorEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/monitors/relations")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk());
	}

	@Test
	void admin_canHit_monitorEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/monitors/relations")
				.with(httpBasic("admin", "admin")))
			.andExpect(status().isOk());
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@Test
	void monitor_canCreatePenpal_withExistingParentHelper() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/penpal/monitors/penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_EXISTING_PARENT_HELPER))
				.with(MONITOR_AUTH))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		Long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		PenpalMonitorView expected = new PenpalMonitorView(
			newId,
			NEW_PENPAL_WITH_EXISTING_PARENT_HELPER.firstName(),
			NEW_PENPAL_WITH_EXISTING_PARENT_HELPER.lastName(),
			NEW_PENPAL_WITH_EXISTING_PARENT_HELPER.age(),
			NEW_PENPAL_WITH_EXISTING_PARENT_HELPER.state(),
			NEW_PENPAL_WITH_EXISTING_PARENT_HELPER.biography(),
			UserFullView.of(HUGO)
		);

		mockMvc.perform(get(URI.create(location))
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_canCreatePenpal_withNewParentHelper() throws Exception {

		MvcResult created = mockMvc.perform(post("/api/penpal/monitors/penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_NEW_PARENT_HELPER))
				.with(MONITOR_AUTH))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		Long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		PenpalMonitorView expected = new PenpalMonitorView(
			newId,
			NEW_PENPAL_WITH_NEW_PARENT_HELPER.firstName(),
			NEW_PENPAL_WITH_NEW_PARENT_HELPER.lastName(),
			NEW_PENPAL_WITH_NEW_PARENT_HELPER.age(),
			NEW_PENPAL_WITH_NEW_PARENT_HELPER.state(),
			NEW_PENPAL_WITH_NEW_PARENT_HELPER.biography(),
			new UserFullView (
				newId - 1, // creates the parent first
				NEW_PENPAL_WITH_NEW_PARENT_HELPER.parentHelper().firstName(),
				NEW_PENPAL_WITH_NEW_PARENT_HELPER.parentHelper().lastName(),
				NEW_PENPAL_WITH_NEW_PARENT_HELPER.parentHelper().email(),
				NEW_PENPAL_WITH_NEW_PARENT_HELPER.parentHelper().phone(),
				NEW_PENPAL_WITH_NEW_PARENT_HELPER.parentHelper().whatsapp(),
				RoleEnum.PARENT_HELPER
			)
		);

		mockMvc.perform(get(URI.create(location))
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_cannotCreatePenpal_withoutParentHelper() throws Exception {

		mockMvc.perform(post("/api/penpal/monitors/penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_NULL_PARENT_HELPER))
				.with(MONITOR_AUTH))
			.andExpect(status().isBadRequest());
	}

	@Test
	void monitor_canCreateParentHelper() throws Exception {

		MvcResult created = mockMvc.perform(post("/api/penpal/monitors/parent-helpers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PARENT_HELPER))
				.with(MONITOR_AUTH))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		Long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		UserFullView expected = new UserFullView(
			newId,
			NEW_PARENT_HELPER.firstName(),
			NEW_PARENT_HELPER.lastName(),
			NEW_PARENT_HELPER.email(),
			NEW_PARENT_HELPER.phone(),
			NEW_PARENT_HELPER.whatsapp(),
			RoleEnum.PARENT_HELPER
		);

		mockMvc.perform(get(URI.create(location))
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	///////////////////////////////////////////////////////////////
	// READ

	@Test
	void monitor_readsRelationshipMap() throws Exception {
		// grouped by guardian, ordered most -> fewest penpals — the exact shape the frontend map draws:
		// each guardian once, branching to their penpals, each penpal with its companion.
		// Deduped: Pat guards one penpal from each chat, so her two rows already cover
		// all four chatting penpals + both chats. Helen & Hugo would only repeat
		// Bob/Carlos (already shown as companions), so they drop out entirely.
		// Quinn & Rosa each guard one of the chatless pair (Omar / Priya) — shown with
		// no companion. Nia guards no penpal at all, so she is a lone (empty) node.
		MonitorMapRelationshipView expected = new MonitorMapRelationshipView(List.of(
			new GuardianMapRelationshipView(UserFullView.of(PAT), List.of(
				new PenpalWithCompanion(PenpalMonitorView.of(ALICE), PenpalMonitorView.of(BOB)),
				new PenpalWithCompanion(PenpalMonitorView.of(DIANA), PenpalMonitorView.of(CARLOS)))),
			new GuardianMapRelationshipView(UserFullView.of(QUINN), List.of(
				new PenpalWithCompanion(PenpalMonitorView.of(OMAR), null))),
			new GuardianMapRelationshipView(UserFullView.of(ROSA), List.of(
				new PenpalWithCompanion(PenpalMonitorView.of(PRIYA), null))),
			new GuardianMapRelationshipView(UserFullView.of(NIA), List.of())));

		mockMvc.perform(get("/api/penpal/monitors/relations")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_readsAllUsers() throws Exception {
		// ordered by role
		List<UserFullView> expected = List.of(
			UserFullView.of(ADAM),    // ADMIN
			UserFullView.of(MONA),    // MONITOR
			UserFullView.of(HELEN),   // PARENT_HELPER
			UserFullView.of(HUGO),
			UserFullView.of(PAT),
			UserFullView.of(NIA),
			UserFullView.of(QUINN),
			UserFullView.of(ROSA),
			UserFullView.of(ALICE),   // PENPAL
			UserFullView.of(BOB),
			UserFullView.of(CARLOS),
			UserFullView.of(DIANA),
			UserFullView.of(OMAR),
			UserFullView.of(PRIYA));

		mockMvc.perform(get("/api/penpal/monitors/all-users")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_readsPenpal() throws Exception {
		PenpalMonitorView expected = PenpalMonitorView.of(BOB);

		mockMvc.perform(get("/api/penpal/monitors/penpals/" + BOB.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void monitor_readsParentHelper() throws Exception {
		UserFullView expected = UserFullView.of(HUGO);

		mockMvc.perform(get("/api/penpal/monitors/parent-helpers/" + HUGO.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void monitor_readsMonitor() throws Exception {
		UserFullView expected = UserFullView.of(MONA);

		mockMvc.perform(get("/api/penpal/monitors/monitors/" + MONA.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void monitor_readsAdmin() throws Exception {
		UserFullView expected = UserFullView.of(ADAM);

		mockMvc.perform(get("/api/penpal/monitors/admins/" + ADAM.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@Test
	void monitor_canReassignPenpal_ToDifferentExistingParentHelper()  throws Exception {

		CreatePenpalRequest bobUpdate = new CreatePenpalRequest(BOB.getFirstName(),
			BOB.getLastName(), 4, BOB.getState(), BOB.getBiography(), HUGO.getId(), null);
		// change in age will not take affect, the only change allowed is helper

		mockMvc.perform(put("/api/penpal/monitors/reassign-penpal/" + BOB.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bobUpdate))
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent());

		PenpalMonitorView expected = new PenpalMonitorView( BOB.getId(), BOB.getFirstName(), BOB.getLastName(), BOB.getAge(), BOB.getState(), BOB.getBiography(), UserFullView.of(HUGO));


		mockMvc.perform(get("/api/penpal/monitors/penpals/" + BOB.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_canReassignPenpal_ToNewParentHelper()  throws Exception {

		CreatePenpalRequest bobUpdate = new CreatePenpalRequest(BOB.getFirstName(),
			BOB.getLastName(), 4, BOB.getState(), BOB.getBiography(), null, NEW_PARENT_HELPER);
		// change in age will not take affect, the only change allowed is helper

		mockMvc.perform(put("/api/penpal/monitors/reassign-penpal/" + BOB.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bobUpdate))
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent());

		MvcResult res = mockMvc.perform(get("/api/penpal/monitors/penpals/" + BOB.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andReturn();

		int guardianId = com.jayway.jsonpath.JsonPath.read(res.getResponse().getContentAsString(), "$.parentHelper.id");
		assertThat(guardianId).isNotEqualTo(HELEN.getId().intValue());

		PenpalMonitorView expected = new PenpalMonitorView(
			BOB.getId(), BOB.getFirstName(), BOB.getLastName(), BOB.getAge(), BOB.getState(), BOB.getBiography(),
			new UserFullView((long) guardianId,
				NEW_PARENT_HELPER.firstName(), NEW_PARENT_HELPER.lastName(), NEW_PARENT_HELPER.email(),
				NEW_PARENT_HELPER.phone(), NEW_PARENT_HELPER.whatsapp(), RoleEnum.PARENT_HELPER));

		assertThat(res.getResponse().getContentAsString())
			.isEqualTo(objectMapper.writeValueAsString(expected));
	}

	@Test
	void monitor_cannotReassignPenpal_ToNullParentHelper()  throws Exception {

		CreatePenpalRequest bobUpdate = new CreatePenpalRequest(BOB.getFirstName(),
			BOB.getLastName(), BOB.getAge(), BOB.getState(), BOB.getBiography(), null, null);

		mockMvc.perform(put("/api/penpal/monitors/reassign-penpal/" + BOB.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bobUpdate))
				.with(MONITOR_AUTH))
			.andExpect(status().isBadRequest());
	}

	@Test
	void monitor_canUpdatePenpal() throws Exception {
		CreatePenpalRequest bobUpdate = new CreatePenpalRequest(BOB.getFirstName(), "Stewart", BOB.getAge(), BOB.getState(), BOB.getBiography(), BOB.getParentHelper().getId(), null);

		mockMvc.perform(put("/api/penpal/monitors/penpals/" + BOB.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bobUpdate))
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent())
			.andReturn();

		PenpalMonitorView expected = new PenpalMonitorView( BOB.getId(), BOB.getFirstName(), "Stewart", BOB.getAge(), BOB.getState(), BOB.getBiography(), UserFullView.of(HELEN));

		mockMvc.perform(get("/api/penpal/monitors/penpals/" + BOB.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_canUpdateParentHelper() throws Exception {
		CreateAppUserRequest hugoUpdate = new CreateAppUserRequest(HUGO.getFirstName(), "Suarez", HUGO.getEmail(), HUGO.getPhone(), HUGO.getWhatsapp());

		mockMvc.perform(put("/api/penpal/monitors/parent-helpers/" + HUGO.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(hugoUpdate))
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent())
			.andReturn();

		UserFullView expected = new UserFullView(HUGO.getId(), HUGO.getFirstName(), "Suarez", HUGO.getEmail(), HUGO.getPhone(), HUGO.getWhatsapp(), HUGO.getRole());

		mockMvc.perform(get("/api/penpal/monitors/parent-helpers/" + HUGO.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@Test
	void monitor_canCreate_BlastMessage() throws Exception {
		CreateBlastMessageRequest blastMessage = new CreateBlastMessageRequest("ANNOUNCEMENT");

		MvcResult created = mockMvc.perform(post("/api/penpal/monitors/messages")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(blastMessage))
				.with(httpBasic(MONA.getAuthId(), MONA.getAuthId())))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		MvcResult res = mockMvc.perform(get(URI.create(location))
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andReturn();

		MessageMonitorView actual = objectMapper.readValue(
			res.getResponse().getContentAsString(), MessageMonitorView.class);

		MessageMonitorView expected = new MessageMonitorView(
			newId,
			blastMessage.text(),
			new PenpalMonitorView(MONA.getId(), MONA.getFirstName(), MONA.getLastName(), null, null, "~ a monitor", null),
			actual.createTime(),
			actual.chat(),
			true,
			actual.approvedTime()
		);

		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@Test
	void monitor_canCreate_Chat() throws Exception {
		CreateChatRequest body = new CreateChatRequest(List.of(OMAR.getId(), PRIYA.getId()), true);

		MvcResult created = mockMvc.perform(post("/api/penpal/monitors/chats")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
				.with(MONITOR_AUTH))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");

		mockMvc.perform(get(URI.create(location))
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.active").value(true))
			.andExpect(jsonPath("$.members[*].id",
				containsInAnyOrder(OMAR.getId().intValue(), PRIYA.getId().intValue())));
	}

	///////////////////////////////////////////////////////////////
	// READ

	@Test
	void monitor_canRead_Any_Message() throws Exception {

		MessageMonitorView expected = new MessageMonitorView(MSG_1.getId(), MSG_1.getText(), PenpalMonitorView.of(MSG_1.getPenpalAuthor()), MSG_1.getCreateTime(), ChatMonitorView.of(MSG_1.getChat()), MSG_1.getApproved(), MSG_1.getApprovedTime());

		mockMvc.perform(get("/api/penpal/monitors/messages/" +  MSG_1.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true))
			.andReturn();

	}

	@Test
	void monitor_canRead_AnyEntireChat() throws Exception {

		ChatMonitorView expected = ChatMonitorView.of(CHAT_2);

		mockMvc.perform(get("/api/penpal/monitors/chats/" +  CHAT_2.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true))
			.andReturn();

	}

	@Test
	void monitor_canRead_listOfAll_Messages() throws Exception {

		List<MessageMonitorView> expected = List.of(
			MessageMonitorView.of(MSG_1),
			MessageMonitorView.of(MSG_2),
			MessageMonitorView.of(MSG_3),
			MessageMonitorView.of(MSG_6),
			MessageMonitorView.of(MSG_7),
			MessageMonitorView.of(MSG_8),
			MessageMonitorView.of(MSG_9),
			MessageMonitorView.of(MSG_10),
			MessageMonitorView.of(MSG_11),
			MessageMonitorView.of(BLAST_1),
			MessageMonitorView.of(BLAST_2));

		mockMvc.perform(get("/api/penpal/monitors/messages/all")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), false)) // can be out of order
			.andReturn();

	}

	@Test
	void monitor_canRead_listOfAll_Unapproved_Messages() throws Exception {

		List<MessageMonitorView> expected = List.of(
			MessageMonitorView.of(MSG_2), MessageMonitorView.of(MSG_8), MessageMonitorView.of(MSG_10));

		mockMvc.perform(get("/api/penpal/monitors/messages/unapproved")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true))
			.andReturn();

	}

	@Test
	void monitor_canRead_listOfAll_Chats() throws Exception {

		// /chats/all now returns each chat bundled with its messages (MonitorChatMessageView)
		List<MonitorChatMessageView> expected = List.of(
			new MonitorChatMessageView(ChatMonitorView.of(CHAT_1),
				List.of(MessageMonitorView.of(MSG_1), MessageMonitorView.of(MSG_2), MessageMonitorView.of(MSG_6),
					MessageMonitorView.of(MSG_7), MessageMonitorView.of(MSG_8), MessageMonitorView.of(BLAST_1))),
			new MonitorChatMessageView(ChatMonitorView.of(CHAT_2),
				List.of(MessageMonitorView.of(MSG_3), MessageMonitorView.of(MSG_9), MessageMonitorView.of(MSG_10),
					MessageMonitorView.of(MSG_11), MessageMonitorView.of(BLAST_2))));

		mockMvc.perform(get("/api/penpal/monitors/chats/all")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), false)) //can be out of order
			.andReturn();

	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@Test
	void monitor_canEditText_AnyMessage() throws Exception {
		UpdateMessageTextOnlyRequest message1Update = new UpdateMessageTextOnlyRequest("There are no strings on me!");

		mockMvc.perform(put("/api/penpal/monitors/messages/" + MSG_2.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message1Update))
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent())
			.andReturn();

		MessageMonitorView expected = new MessageMonitorView(
			MSG_2.getId(),
			message1Update.text(),
			PenpalMonitorView.of(MSG_2.getPenpalAuthor()),
			MSG_2.getCreateTime(),
			ChatMonitorView.of(MSG_2.getChat()),
			MSG_2.getApproved(),
			MSG_2.getApprovedTime());

		mockMvc.perform(get("/api/penpal/monitors/messages/" + MSG_2.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_canEditApproval_AnyMessage() throws Exception {
		ApprovalMessageRequest message1Update = new ApprovalMessageRequest(true);

		mockMvc.perform(patch("/api/penpal/monitors/messages/" + MSG_1.getId() + "/approval")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message1Update))
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent())
			.andReturn();

		MvcResult res = mockMvc.perform(get("/api/penpal/monitors/messages/" + MSG_1.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andReturn();

		MessageMonitorView actual = objectMapper.readValue(
			res.getResponse().getContentAsString(), MessageMonitorView.class);

		MessageMonitorView expected = new MessageMonitorView(
			MSG_1.getId(),
			MSG_1.getText(),
			PenpalMonitorView.of(MSG_1.getPenpalAuthor()),
			MSG_1.getCreateTime(),
			ChatMonitorView.of(MSG_1.getChat()),
			true,
			actual.approvedTime());

		mockMvc.perform(get("/api/penpal/monitors/messages/" + MSG_1.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_canEditRemove_AnyMessage_FromChat() throws Exception {
		mockMvc.perform(delete("/api/penpal/monitors/messages/" + MSG_2.getId() + "/chats/" + MSG_2.getChat().getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent())
			.andReturn();

		MessageMonitorView expected = new MessageMonitorView(
			MSG_2.getId(),
			MSG_2.getText(),
			PenpalMonitorView.of(MSG_2.getPenpalAuthor()),
			MSG_2.getCreateTime(),
			null,
			MSG_2.getApproved(),
			MSG_2.getApprovedTime());

		mockMvc.perform(get("/api/penpal/monitors/messages/" + MSG_2.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

		ChatMonitorView expectedChat = ChatMonitorView.of(MSG_2.getChat());

		mockMvc.perform(get("/api/penpal/monitors/chats/" +  MSG_2.getChat().getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expectedChat), true))
			.andReturn();
	}

	@Test
	void monitor_canEditDeactivate_AnyChat() throws Exception {
		mockMvc.perform(patch("/api/penpal/monitors/chats/" + CHAT_1.getId() + "/activation")
				.param("active", "false")
				.with(MONITOR_AUTH))
			.andExpect(status().isNoContent());

		ChatMonitorView expected = new ChatMonitorView(
			CHAT_1.getId(),
			List.of(PenpalMonitorView.of(ALICE), PenpalMonitorView.of(BOB)),
			false);

		mockMvc.perform(get("/api/penpal/monitors/chats/" + CHAT_1.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

}
