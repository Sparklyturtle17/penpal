package com.penpals.access.parenthelper;

import com.penpals.ControllerTestBase;
import com.penpals.chat.dto.ChatViews.*;
import com.penpals.chat.dto.MessageRequests.*;
import com.penpals.chat.dto.MessageViews.*;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static com.penpals.SeedData.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class PenpalControllerTest extends ControllerTestBase {

	RequestPostProcessor PARENT_HELPER_AUTH = httpBasic("parent_helper", "parent_helper");

	//╔═════════════════════════════════════════════════════════╗
	//║                           AUTH                          ║
	//╚═════════════════════════════════════════════════════════╝

	@Test
	void guardianActingAs_TheirPenpal_canHit_PenpalEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/penpals/relations")
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk());
	}

	@Test
	void guardianActingAs_NotTheirPenpal_cannotHit_PenpalEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/penpals/relations")
				.header(ActingAsPenpalFilter.HEADER, CARLOS.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void guardianActingAs_TheirPenpal_cannotHit_ParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isForbidden());
	}


	@Test
	void guardianActingAs_TheirPenpal_cannotHit_MonitorEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/monitors/penpals/1")
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isForbidden());
	}

	@Test
	void guardianActingAs_TheirPenpal_cannotHit_AdminEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/admins/penpals/1")
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isForbidden());
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	///////////////////////////////////////////////////////////////
	// READ

	@Test
	void penpal_canReadMe() throws Exception {
		PenpalBioView expected = PenpalBioView.of(CARLOS);

		mockMvc.perform(get("/api/penpal/penpals/me")
				.header(ActingAsPenpalFilter.HEADER, CARLOS.getId())
				.with(httpBasic(HUGO.getAuthId(), HUGO.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void guardianActingAs_TheirPenpal_readsRelationshipMap() throws Exception {
		PenpalMapRelationshipView expected = new PenpalMapRelationshipView(
			PenpalBioView.of(BOB), PenpalBioView.of(ALICE));

		mockMvc.perform(get("/api/penpal/penpals/relations")
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@Test
	void guardianActingAs_TheirPenpal_canCreateMessage() throws Exception {
		CreateNewMessageRequest req = new CreateNewMessageRequest(MSG_1.getText(), MSG_1.getChat().getId());

		MvcResult created = mockMvc.perform(post("/api/penpal/penpals/messages")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))
				.header(ActingAsPenpalFilter.HEADER, ALICE.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		MvcResult res = mockMvc.perform(get(URI.create(location))
				.header(ActingAsPenpalFilter.HEADER, ALICE.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isOk())
			.andReturn();

		MessageSimpleView actual = objectMapper.readValue(
			res.getResponse().getContentAsString(), MessageSimpleView.class);

		MessageSimpleView expected = new MessageSimpleView(
			newId,
			req.text(),
			PenpalBioView.of(MSG_1.getPenpalAuthor()),
			actual.createTime());

		assertThat(actual).isEqualTo(expected);
	}

	///////////////////////////////////////////////////////////////
	// READ

	@Test
	void guardianActingAs_TheirPenpal_canRead_Mine_Message() throws Exception {

		MessageSimpleView expected = new MessageSimpleView(MSG_1.getId(), MSG_1.getText(), PenpalBioView.of(MSG_1.getPenpalAuthor()), MSG_1.getCreateTime());

		mockMvc.perform(get("/api/penpal/penpals/messages/" +  MSG_1.getId())
				.header(ActingAsPenpalFilter.HEADER, ALICE.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true))
			.andReturn();

	}

	@Test
	void guardianActingAs_TheirPenpal_canRead_Companion_Message() throws Exception {

		MessageSimpleView expected = new MessageSimpleView(MSG_1.getId(), MSG_1.getText(), PenpalBioView.of(MSG_1.getPenpalAuthor()), MSG_1.getCreateTime());

		mockMvc.perform(get("/api/penpal/penpals/messages/" +  MSG_1.getId())
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true))
			.andReturn();

	}

	@Test
	void guardianActingAs_TheirPenpal_cannotRead_NotMyChat_Message() throws Exception {

		mockMvc.perform(get("/api/penpal/penpals/messages/" +  MSG_1.getId())
				.header(ActingAsPenpalFilter.HEADER, CARLOS.getId())
				.with(httpBasic(HUGO.getAuthId(), HUGO.getAuthId())))
			.andExpect(status().isNotFound());

	}

	@Test
	void guardianActingAs_TheirPenpal_canRead_EntireChat() throws Exception {

		ChatSimpleView expected = ChatSimpleView.of(CHAT_2);

		mockMvc.perform(get("/api/penpal/penpals/chats/" +  CHAT_2.getId())
				.header(ActingAsPenpalFilter.HEADER, CARLOS.getId())
				.with(httpBasic(HUGO.getAuthId(), HUGO.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true))
			.andReturn();

	}

	@Test
	void guardianActingAs_TheirPenpal_cannotRead_NotMyChat() throws Exception {

		mockMvc.perform(get("/api/penpal/penpals/chats/" +  CHAT_2.getId())
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isNotFound());

	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@Test
	void guardianActingAs_TheirPenpal_canUpdate_Mine_Message_Text() throws Exception {
		UpdateMessageTextOnlyRequest message1Update = new UpdateMessageTextOnlyRequest("There are no strings on me!");

		mockMvc.perform(put("/api/penpal/penpals/messages/" + MSG_1.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message1Update))
				.header(ActingAsPenpalFilter.HEADER, ALICE.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isNoContent())
			.andReturn();

		MessageSimpleView expected = new MessageSimpleView(
			MSG_1.getId(),
			message1Update.text(),
			PenpalBioView.of(MSG_1.getPenpalAuthor()),
			MSG_1.getCreateTime());

		mockMvc.perform(get("/api/penpal/penpals/messages/" + MSG_1.getId())
				.header(ActingAsPenpalFilter.HEADER, ALICE.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void guardianActingAs_TheirPenpal_cannotUpdate_NotMine_Message_Text() throws Exception {
		UpdateMessageTextOnlyRequest message1Update = new UpdateMessageTextOnlyRequest("There are no strings on me!");

		mockMvc.perform(put("/api/penpal/penpals/messages/" + MSG_1.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message1Update))
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void guardianActingAs_TheirPenpal_cannotUpdate_Mine_Message_Approval() throws Exception {
		ApprovalMessageRequest message1Update = new ApprovalMessageRequest(MSG_1.getId(), true);

		mockMvc.perform(put("/api/penpal/penpals/messages/" + MSG_1.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message1Update))
				.header(ActingAsPenpalFilter.HEADER, ALICE.getId())
				.with(httpBasic(PAT.getAuthId(), PAT.getAuthId())))
			.andExpect(status().isBadRequest());
	}
}
