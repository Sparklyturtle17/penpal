package com.penpals.access.parenthelper;

import com.penpals.ControllerTestBase;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static com.penpals.SeedData.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
		var expected = new PenpalMapRelationshipView(
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

	///////////////////////////////////////////////////////////////
	// READ

	///////////////////////////////////////////////////////////////
	// UPDATE
}
