package com.penpals.access;

import com.penpals.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")                       // Basic auth + in-memory users + H2
public class SecurityTest extends ControllerTestBase {


	// ---- authentication required by default ----

	@Test
	void protectedEndpoint_noCredentials_is401() throws Exception {
		mockMvc.perform(get("/api/users/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_badPassword_is401() throws Exception {
		mockMvc.perform(get("/api/users/me")
				.with(httpBasic("admin", "wrong")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpoint_validCredentials_is200() throws Exception {
		mockMvc.perform(get("/api/users/me")
				.with(httpBasic("admin", "admin")))
			.andExpect(status().isOk());
	}


	// ---- method security enforced: authenticated but wrong role -> 403 ----

	@Test
	void authenticatedButWrongRole_is403() throws Exception {
		mockMvc.perform(get("/api/penpal/admins/penpal/1").with(httpBasic("penpal", "penpal")))
			.andExpect(status().isForbidden());
	}


	// ---- unknown endpoints are still locked down ----

	@Test
	void unknownEndpoint_noCredentials_is401() throws Exception {
		// security runs before routing, so an unknown path unauthenticated is 401, NOT 404 —
		// no leaking which paths exist to anonymous callers
		mockMvc.perform(get("/api/does-not-exist"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void unknownEndpoint_authenticated_is404() throws Exception {
		// once authenticated, a genuinely missing path is a real 404
		mockMvc.perform(get("/api/does-not-exist").with(httpBasic("admin", "admin")))
			.andExpect(status().isNotFound());
	}

}