package com.penpals.access.admin;

import com.penpals.ControllerTestBase;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.dto.CreateAppUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static com.penpals.SeedData.*;
import static com.penpals.TestFixtures.Users.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class AdminControllerTest extends ControllerTestBase {

	RequestPostProcessor ADMIN_AUTH = httpBasic("admin", "admin");

	CreateAppUserRequest NEW_MONITOR = APP_USER_A;


	//╔═════════════════════════════════════════════════════════╗
	//║                           AUTH                          ║
	//╚═════════════════════════════════════════════════════════╝

	@Test
	void guardianActingAs_theirPenpal_cannotHit_adminEndpoint() throws Exception {
		mockMvc.perform(post("/api/penpal/admins/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_MONITOR))
				.header(ActingAsPenpalFilter.HEADER, BOB.getId())
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_cannotHit_adminEndpoint() throws Exception {
		mockMvc.perform(post("/api/penpal/admins/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_MONITOR))
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isForbidden());
	}

	@Test
	void monitor_cannotHit_adminEndpoint() throws Exception {
		mockMvc.perform(post("/api/penpal/admins/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_MONITOR))
				.with(httpBasic("monitor", "monitor")))
			.andExpect(status().isForbidden());
	}

	@Test
	void admin_canHit_adminEndpoint() throws Exception {
		mockMvc.perform(post("/api/penpal/admins/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_MONITOR))
				.with(ADMIN_AUTH))
			.andExpect(status().isCreated());
	}

	//╔═════════════════════════════════════════════════════════╗
	//║                          USERS                          ║
	//╚═════════════════════════════════════════════════════════╝

	///////////////////////////////////////////////////////////////
	// CREATE

	@Test
	void admin_canCreateMonitor() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/penpal/admins/monitors")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_MONITOR))
				.with(ADMIN_AUTH))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");
		Long newId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

		UserFullView expected = new UserFullView(newId, NEW_MONITOR.firstName(), NEW_MONITOR.lastName(), NEW_MONITOR.email(), NEW_MONITOR.phone(), NEW_MONITOR.whatsapp(), RoleEnum.MONITOR);

		mockMvc.perform(get("/api/penpal/monitors/monitors/" + newId)
				.with(ADMIN_AUTH))
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
