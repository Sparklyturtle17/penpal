package com.penpals;

import com.penpals.common.State;
import com.penpals.users.dto.CreateAppUserRequest;
import com.penpals.users.dto.CreatePenpalRequest;

/** Reusable request fixtures for controller/service tests, grouped by domain. */
public final class TestFixtures {

	private TestFixtures() {
	}

	public static final class Users {
		private Users() {}

		public static final CreateAppUserRequest PARENT_HELPER_A =
			new CreateAppUserRequest("John", "Humphreys", "test@humphreys.com", "+14155552671", "+14155552671");
	}

	public static final class Penpals {
		private Penpals() {}

		/** No guardian named — the endpoint should attach the caller. */
		public static final CreatePenpalRequest PENPAL_A =
			new CreatePenpalRequest("Robbie", "Daniels", 12, State.TX, "Howdy, y'all!", null, null);

		/** Names the dev caller (Helen, id 5) as guardian. */
		public static final CreatePenpalRequest PENPAL_B =
			new CreatePenpalRequest("Betty", "Sue", 11, State.MD, "Good evening", 5L, null);

		/** Names a DIFFERENT guardian (Hugo, id 6) — should be forbidden for Helen. */
		public static final CreatePenpalRequest PENPAL_C =
			new CreatePenpalRequest("Larry", "Sienfeld", 10, State.VT, "Haha!", 6L, null);

		/** Mints a brand-new guardian inline — forbidden for a parent/helper. */
		public static final CreatePenpalRequest PENPAL_D =
			new CreatePenpalRequest("Joshua", "Abbot", 9, State.CA, "Lets go camping", null, Users.PARENT_HELPER_A);
	}

	// Add these once the DTOs exist:
	//
	// public static final class Messages {
	//     private Messages() {}
	//     public static final CreateMessageRequest ... ;
	// }
	//
	// public static final class Chats {
	//     private Chats() {}
	//     public static final CreateChatRequest ... ;
	// }
}