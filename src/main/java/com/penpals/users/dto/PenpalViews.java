package com.penpals.users.dto;


import com.penpals.common.State;
import com.penpals.users.dto.AppUserViews.*;
import com.penpals.users.penpal.Penpal;

public interface PenpalViews {

	record PenpalMonitorView(
		Long id,
		String firstName,
		String lastName,
		Integer age,
		State state,
		String biography,
		UserFullView parentHelper
	) {
		public static PenpalMonitorView of(Penpal p) {
			return new PenpalMonitorView(
				p.getId(),
				p.getFirstName(),
				p.getLastName(),
				p.getAge(),
				p.getState(),
				p.getBiography(),
				p.getParentHelper() == null ? null : UserFullView.of(p.getParentHelper())
			);
		}
	}

	record PenpalBioView(
		Long id,
		String firstName,
		Integer age,
		State state,
		String biography
	) {
		public static PenpalBioView of (Penpal p) {
			return new PenpalBioView(
				p.getId(),
				p.getFirstName(),
				p.getAge(),
				p.getState(),
				p.getBiography()
			);
		}
	}

}
