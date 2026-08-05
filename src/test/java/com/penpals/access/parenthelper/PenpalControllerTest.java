package com.penpals.access.parenthelper;

import com.penpals.ControllerTestBase;
import com.penpals.common.config.ActingAsPenpalFilter;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PenpalControllerTest extends ControllerTestBase {

	@Test
	void guardianActingAsTheirPenpal_canHitPenpalEndpoint() throws Exception {
		mockMvc.perform(get("/api/penpal/penpals/relations")
				.header(ActingAsPenpalFilter.HEADER, "2")
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isOk());
	}


	@Test
	void guardianActingAsTheirPenpal_cannotHitMonitorEndpoint_is403() throws Exception {
		mockMvc.perform(get("/api/penpal/monitors/penpals/1")
				.header(ActingAsPenpalFilter.HEADER, "2")
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isForbidden());
	}

	@Test
	void guardianActingAsTheirPenpal_cannotHitAdminEndpoint_is403() throws Exception {
		mockMvc.perform(get("/api/penpal/admins/penpals/1")
				.header(ActingAsPenpalFilter.HEADER, "2")
				.with(httpBasic("parent_helper", "parent_helper")))
			.andExpect(status().isForbidden());
	}
}
