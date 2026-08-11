package com.penpals.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penpals.common.State;
import com.penpals.users.AppUser;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.AppUserViews.UserFullView;
import com.penpals.users.dto.AppUserViews.UserSummaryView;
import com.penpals.users.dto.PenpalViews.PenpalMonitorView;
import com.penpals.users.dto.PenpalViews.PenpalBioView;
import com.penpals.users.penpal.Penpal;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ViewMappingTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	void userFullView_mapsEveryField() throws Exception {
		AppUser u = new AppUser();
		u.setId(5L); u.setFirstName("first"); u.setLastName("last");   // distinct values catch transposition
		u.setEmail("e@x.com"); u.setPhone("111"); u.setWhatsapp("222");
		u.setRole(RoleEnum.PARENT_HELPER);

		UserFullView v = UserFullView.of(u);

		assertThat(v.id()).isEqualTo(5L);
		assertThat(v.firstName()).isEqualTo("first");
		assertThat(v.lastName()).isEqualTo("last");
		assertThat(v.email()).isEqualTo("e@x.com");
		assertThat(v.phone()).isEqualTo("111");
		assertThat(v.whatsapp()).isEqualTo("222");
		assertThat(v.role()).isEqualTo(RoleEnum.PARENT_HELPER);

		Map<String, Object> keys = mapper.readValue(
			mapper.writeValueAsString(v), new TypeReference<>() {});

		assertThat(keys.keySet())
			.containsExactlyInAnyOrder("id", "firstName", "lastName", "email", "phone", "whatsapp", "role");
	}

	@Test
	void userSummaryView_mapsIdAndNameOnly() throws Exception {
		AppUser u = new AppUser();
		u.setId(9L); u.setFirstName("first"); u.setLastName("last");

		UserSummaryView v = UserSummaryView.of(u);

		assertThat(v.id()).isEqualTo(9L);
		assertThat(v.firstName()).isEqualTo("first");
		assertThat(v.lastName()).isEqualTo("last");

		Map<String, Object> keys = mapper.readValue(
			mapper.writeValueAsString(v), new TypeReference<>() {});

		assertThat(keys.keySet())
			.containsExactlyInAnyOrder("id", "firstName", "lastName");
	}

	@Test
	void penpalAdminView_mapsFieldsAndNestsGuardian() throws Exception {
		AppUser guardian = new AppUser();
		guardian.setId(5L); guardian.setRole(RoleEnum.PARENT_HELPER);

		Penpal p = new Penpal();
		p.setId(2L); p.setFirstName("first"); p.setLastName("last");
		p.setAge(12); p.setState(State.LILONGWE); p.setBiography("bio");
		p.setParentHelper(guardian);

		PenpalMonitorView v = PenpalMonitorView.of(p);

		assertThat(v.id()).isEqualTo(2L);
		assertThat(v.firstName()).isEqualTo("first");
		assertThat(v.lastName()).isEqualTo("last");
		assertThat(v.age()).isEqualTo(12);
		assertThat(v.state()).isEqualTo(State.LILONGWE);
		assertThat(v.biography()).isEqualTo("bio");
		assertThat(v.parentHelper()).isNotNull();
		assertThat(v.parentHelper().id()).isEqualTo(5L);   // nested UserFullView.of ran


		Map<String, Object> keys = mapper.readValue(
			mapper.writeValueAsString(v), new TypeReference<>() {});

		assertThat(keys.keySet())
			.containsExactlyInAnyOrder("id", "firstName", "lastName", "age", "state", "biography", "parentHelper");

		Map<String, Object> parentHelperKeys = mapper.readValue(
			mapper.writeValueAsString(v.parentHelper()), new TypeReference<>() {});

		assertThat(parentHelperKeys.keySet())
			.containsExactlyInAnyOrder("id", "firstName", "lastName", "email", "phone", "whatsapp", "role");
	}

	@Test
	void penpalAdminView_nullGuardian_mapsToNull() throws Exception {   // the branch that would NPE if written wrong
		Penpal p = new Penpal();
		p.setId(1L);
		p.setState(State.CA);
		p.setParentHelper(null);

		assertThat(PenpalMonitorView.of(p).parentHelper()).isNull();
	}

	@Test
	void penpalBioView_mapsReducedFields() throws Exception {
		Penpal p = new Penpal();
		p.setId(1L); p.setFirstName("first"); p.setAge(11);
		p.setState(State.CA); p.setBiography("bio");

		PenpalBioView v = PenpalBioView.of(p);

		assertThat(v.id()).isEqualTo(1L);
		assertThat(v.firstName()).isEqualTo("first");
		assertThat(v.age()).isEqualTo(11);
		assertThat(v.state()).isEqualTo(State.CA);
		assertThat(v.biography()).isEqualTo("bio");

		Map<String, Object> keys = mapper.readValue(
			mapper.writeValueAsString(v), new TypeReference<>() {});

		assertThat(keys.keySet())
			.containsExactlyInAnyOrder("id", "firstName", "age", "state", "biography");
	}
}