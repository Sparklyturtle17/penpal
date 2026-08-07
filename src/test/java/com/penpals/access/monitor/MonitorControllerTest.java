package com.penpals.access.monitor;

import com.penpals.ControllerTestBase;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreateAppUserRequest;
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

import static com.penpals.SeedData.*;
import static com.penpals.TestFixtures.Penpals.*;
import static com.penpals.TestFixtures.Users.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

		PenpalAdminView expected = new PenpalAdminView(
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

		PenpalAdminView expected = new PenpalAdminView(
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
		List<MonitorMapRelationshipView> expected = List.of(
			new MonitorMapRelationshipView(PenpalAdminView.of(ALICE), PenpalAdminView.of(BOB)),
			new MonitorMapRelationshipView(PenpalAdminView.of(CARLOS), PenpalAdminView.of(DIANA)));

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
			UserFullView.of(ALICE),   // PENPAL
			UserFullView.of(BOB),
			UserFullView.of(CARLOS),
			UserFullView.of(DIANA));

		mockMvc.perform(get("/api/penpal/monitors/all-users")
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_readsPenpal() throws Exception {
		var expected = PenpalAdminView.of(BOB);

		mockMvc.perform(get("/api/penpal/monitors/penpals/" + BOB.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void monitor_readsParentHelper() throws Exception {
		var expected = UserFullView.of(HUGO);

		mockMvc.perform(get("/api/penpal/monitors/parent-helpers/" + HUGO.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void monitor_readsMonitor() throws Exception {
		var expected = UserFullView.of(MONA);

		mockMvc.perform(get("/api/penpal/monitors/monitors/" + MONA.getId())
				.with(MONITOR_AUTH))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));

	}

	@Test
	void monitor_readsAdmin() throws Exception {
		var expected = UserFullView.of(ADAM);

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

		PenpalAdminView expected = new PenpalAdminView( BOB.getId(), BOB.getFirstName(), BOB.getLastName(), BOB.getAge(), BOB.getState(), BOB.getBiography(), UserFullView.of(HUGO));


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

		PenpalAdminView expected = new PenpalAdminView(
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

		PenpalAdminView expected = new PenpalAdminView( BOB.getId(), BOB.getFirstName(), "Stewart", BOB.getAge(), BOB.getState(), BOB.getBiography(), UserFullView.of(HELEN));

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

	///////////////////////////////////////////////////////////////
	// READ

	///////////////////////////////////////////////////////////////
	// UPDATE
}
