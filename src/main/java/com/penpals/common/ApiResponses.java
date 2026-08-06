package com.penpals.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Shared helpers for building HTTP responses consistently across controllers. */
public final class ApiResponses {

	private ApiResponses() {
	}

	/**
	 * 201 Created, no body, with a Location header of {currentRequestUri}/{id}.
	 * Assumes GET-by-id lives at {POST path}/{id}
	 * (e.g. POST /my-penpals -> GET /my-penpals/{id}).
	 */
	public static ResponseEntity<Void> created(Object id) {
		URI location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(id)
			.toUri();
		return ResponseEntity.created(location).build();
	}
}