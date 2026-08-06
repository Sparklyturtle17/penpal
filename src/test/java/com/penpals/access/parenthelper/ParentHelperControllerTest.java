package com.penpals.access.parenthelper;

import com.penpals.ControllerTestBase;
import com.penpals.common.State;
import com.penpals.common.config.ActingAsPenpalFilter;
import com.penpals.users.dto.CreatePenpalRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static com.penpals.TestFixtures.Penpals.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class ParentHelperControllerTest extends ControllerTestBase {

	CreatePenpalRequest NEW_PENPAL_WITH_NULL = PENPAL_A;
	CreatePenpalRequest NEW_PENPAL_WITH_SELF = PENPAL_B;
	CreatePenpalRequest NEW_PENPAL_WITH_OTHER_PARENT_HELPER = PENPAL_C;
	CreatePenpalRequest NEW_PENPAL_WITH_NEW_PARENT_HELPER = PENPAL_D;

	// AUTH

	@Test
	void guardianActingAsTheirPenpal_cannotHitParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.header(ActingAsPenpalFilter.HEADER, "2")
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isForbidden());
	}

	@Test
	void parentHelper_canHitParentHelperEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/parent-helpers/my-penpals-companions")
				.with(httpBasic("parent_helper", "parent_helper")))
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

	// CREATE

	@Test
	void parentHelper_canCreatePenpal_WithSelf() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_SELF))
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");

		mockMvc.perform(get(URI.create(location))
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value(NEW_PENPAL_WITH_SELF.firstName()))
			.andExpect(jsonPath("$.state").value(NEW_PENPAL_WITH_SELF.state().name()))
			.andExpect(jsonPath("$.parentHelper.id").value(5));
	}

	@Test
	void parentHelper_canCreatePenpal_AutomaticallySelf() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_NULL))
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isCreated())
			.andExpect(header().exists("Location"))
			.andReturn();

		String location = created.getResponse().getHeader("Location");

		mockMvc.perform(get(URI.create(location))
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value(NEW_PENPAL_WITH_NULL.firstName()))
			.andExpect(jsonPath("$.state").value(NEW_PENPAL_WITH_NULL.state().name()))
			.andExpect(jsonPath("$.parentHelper.id").value(5));
	}

	@Test
	void parentHelper_cannotCreatePenpal_NewParentHelper() throws Exception {
		mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_NEW_PARENT_HELPER))
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isForbidden())
			.andExpect(header().doesNotExist("Location"));
	}

	@Test
	void parentHelper_cannotCreatePenpal_ForOtherParentHelper() throws Exception {
		mockMvc.perform(post("/api/penpal/parent-helpers/my-penpals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(NEW_PENPAL_WITH_OTHER_PARENT_HELPER))
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isForbidden())
			.andExpect(header().doesNotExist("Location"));
	}

	//todo
	// READ


	//todo
	// UPDATE

}
