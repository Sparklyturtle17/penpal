package com.penpals.access.parenthelper;

import com.penpals.ControllerTestBase;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.dto.RelationshipsView.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

import static com.penpals.TestFixtures.Penpals.*;
import static com.penpals.SeedData.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@Transactional
public class ParentHelperControllerTest extends ControllerTestBase {
	
	RequestPostProcessor PARENT_HELPER_AUTH = httpBasic("parent_helper", "parent_helper");

	CreatePenpalRequest NEW_PENPAL_WITH_NULL = PENPAL_A;
	CreatePenpalRequest NEW_PENPAL_WITH_SELF = PENPAL_B;
	CreatePenpalRequest NEW_PENPAL_WITH_OTHER_PARENT_HELPER = PENPAL_C;
	CreatePenpalRequest NEW_PENPAL_WITH_NEW_PARENT_HELPER = PENPAL_D;

	//╔═════════════════════════════════════════════════════════╗
	//║                           AUTH                          ║
	//╚═════════════════════════════════════════════════════════╝

	@Test
	void guardianActingAsTheirPenpal_cannotHitParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.header(ActingAsPenpalFilter.HEADER, "2")
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_canHitParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isOk());
	}

	@Test
	void monitor_cannotHitParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.with(httpBasic("monitor", "monitor")))
			.andExpect(status().isForbidden());
	}

	@Test
	void admin_cannotHitParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.with(httpBasic("admin", "admin")))
			.andExpect(status().isForbidden());
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@Test
	void parentHelper_canCreatePenpal_WithSelf() throws Exception {

		MvcResult created = mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_SELF))
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		Long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		PenpalMonitorView expected = new PenpalMonitorView(
			newId,
			NEW_PENPAL_WITH_SELF.firstName(),
			NEW_PENPAL_WITH_SELF.lastName(),
			NEW_PENPAL_WITH_SELF.age(),
			NEW_PENPAL_WITH_SELF.state(),
			NEW_PENPAL_WITH_SELF.biography(),
			UserFullView.of(HELEN)
		);

		mockMvc.perform(get(URI.create(location))
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void parentHelper_canCreatePenpal_AutomaticallySelf() throws Exception {

		MvcResult created = mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_NULL))
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		Long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		PenpalMonitorView expected = new PenpalMonitorView(
			newId,
			NEW_PENPAL_WITH_NULL.firstName(),
			NEW_PENPAL_WITH_NULL.lastName(),
			NEW_PENPAL_WITH_NULL.age(),
			NEW_PENPAL_WITH_NULL.state(),
			NEW_PENPAL_WITH_NULL.biography(),
			UserFullView.of(HELEN));

		mockMvc.perform(get(URI.create(location))
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void parentHelper_cannotCreatePenpal_NewParentHelper() throws Exception {
		mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_NEW_PARENT_HELPER))
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isForbidden())
			.andExpect(header().doesNotExist("Location"));
	}

	@Test
	void parentHelper_cannotCreatePenpal_ForOtherParentHelper() throws Exception {
		mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_OTHER_PARENT_HELPER))
				.with(PARENT_HELPER_AUTH))
			.andExpect(status().isForbidden())
			.andExpect(header().doesNotExist("Location"));
	}

	///////////////////////////////////////////////////////////////
	// READ

	@Test
	void parentHelper_readsRelationshipMap() throws Exception {
		GuardianMapRelationshipView expected = new GuardianMapRelationshipView(
			UserFullView.of(HELEN),
			List.of(new PenpalWithCompanion(PenpalMonitorView.of(BOB), PenpalBioView.of(ALICE))));

		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void parentHelper_readsPenpal_Mine() throws Exception {
		PenpalMonitorView expected = PenpalMonitorView.of(BOB);

		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals/" + BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void parentHelper_cannotReadPenpal_NotMine() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals/" + CARLOS.getId())
			.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_readsPenpalCompanion_Mine() throws Exception {
		PenpalBioView expected = PenpalBioView.of(ALICE);

		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions/" + BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void parentHelper_cannotReadPenpalCompanion_NotMine() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions/" + CARLOS.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	///////////////////////////////////////////////////////////////
	// UPDATE

	@Test
	void parentHelper_canUpdatePenpal_Mine() throws Exception {
		CreatePenpalRequest bobUpdate = new CreatePenpalRequest(BOB.getFirstName(), "Stewart", BOB.getAge(), BOB.getState(), BOB.getBiography(), null, null);

		mockMvc.perform(put("/api/penpal/parent-helpers/my-penpals/" + BOB.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bobUpdate))
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isNoContent())
			.andReturn();

		PenpalMonitorView expected = new PenpalMonitorView( BOB.getId(), BOB.getFirstName(), "Stewart", BOB.getAge(), BOB.getState(), BOB.getBiography(), UserFullView.of(HELEN));

		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals/" + BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void parentHelper_cannotUpdatePenpal_NotMine() throws Exception {
		CreatePenpalRequest aliceUpdate = new CreatePenpalRequest(ALICE.getFirstName(), "Stewart", ALICE.getAge(), ALICE.getState(), ALICE.getBiography(), null, null);

		mockMvc.perform(put("/api/penpal/parent-helpers/my-penpals/" + ALICE.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(aliceUpdate))
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_cannotReassignPenpal_Mine() throws Exception {
		CreatePenpalRequest reassign = new CreatePenpalRequest(BOB.getFirstName(),
			BOB.getLastName(), BOB.getAge(), BOB.getState(), BOB.getBiography(), HUGO.getId(), null);

		mockMvc.perform(put("/api/penpal/parent-helpers/my-penpals/" + BOB.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reassign))
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_cannotReassignPenpal_NotMine() throws Exception {
		CreatePenpalRequest reassign = new CreatePenpalRequest(CARLOS.getFirstName(),
			CARLOS.getLastName(), CARLOS.getAge(), CARLOS.getState(), CARLOS.getBiography(), HUGO.getId(), null);

		mockMvc.perform(put("/api/penpal/parent-helpers/my-penpals/" + CARLOS.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reassign))
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          CHATS                          ║
	//╚═════════════════════════════════════════════════════════╝

	// should be acting "as their penpal" to perform chat actions

}
