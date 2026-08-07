package com.penpals.access;

import com.penpals.ControllerTestBase;
import com.penpals.users.dto.AppUserViews.*;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static com.penpals.SeedData.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class AppUserControllerTest extends ControllerTestBase {

	@Test
	void penpalHelper_canReadMe() throws Exception {
		UserFullView expected = UserFullView.of(HELEN);

		mockMvc.perform(get("/api/users/me")
				.with(httpBasic(HELEN.getAuthId(), HELEN.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void monitor_canReadMe() throws Exception {
		UserFullView expected = UserFullView.of(MONA);

		mockMvc.perform(get("/api/users/me")
				.with(httpBasic(MONA.getAuthId(), MONA.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}

	@Test
	void admin_canReadMe() throws Exception {
		UserFullView expected = UserFullView.of(ADAM);

		mockMvc.perform(get("/api/users/me")
				.with(httpBasic(ADAM.getAuthId(), ADAM.getAuthId())))
			.andExpect(status().isOk())
			.andExpect(content().json(objectMapper.writeValueAsString(expected), true));
	}
}
