package com.penpals;

import com.penpals.chat.dto.MessageRequests.CreateBlastMessageRequest;
import com.penpals.chat.dto.MessageRequests.CreateNewMessageRequest;
import com.penpals.common.State;
import com.penpals.users.dto.CreateAppUserRequest;
import com.penpals.users.dto.CreatePenpalRequest;

public final class TestFixtures {

	private TestFixtures() {
	}

	public static final class Users {
		private Users() {}

		public static final CreateAppUserRequest APP_USER_A =
			new CreateAppUserRequest("John", "Humphreys", "test@humphreys.com", "+14155552671", "+14155552671");
	}

	public static final class Penpals {
		private Penpals() {}

		public static final CreatePenpalRequest PENPAL_A =
			new CreatePenpalRequest("Robbie", "Daniels", 12, State.TX, "Howdy, y'all!", null, null);

		public static final CreatePenpalRequest PENPAL_B =
			new CreatePenpalRequest("Betty", "Sue", 11, State.MD, "Good evening", 5L, null);

		public static final CreatePenpalRequest PENPAL_C =
			new CreatePenpalRequest("Larry", "Sienfeld", 10, State.VT, "Haha!", 6L, null);

		public static final CreatePenpalRequest PENPAL_D =
			new CreatePenpalRequest("Joshua", "Abbot", 9, State.CA, "", null, Users.APP_USER_A);
	}

	public static final class Messages {
		private Messages() {}

		/** Monitor broadcast to every active chat (text only — sender comes from auth). */
		public static final CreateBlastMessageRequest BLAST =
			new CreateBlastMessageRequest("Reminder: always be kind online!");

		/** A new message in a chat (author + sender come from acting-as + auth, not the body). */
		public static CreateNewMessageRequest inChat(long chatId, String text) {
			return new CreateNewMessageRequest(text, chatId);
		}
	}
}