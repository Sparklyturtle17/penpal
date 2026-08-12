package com.penpals.chat.dto;

import com.penpals.chat.Chat;
import com.penpals.chat.dto.MessageViews.*;
import com.penpals.chat.dto.ChatViews.*;
import com.penpals.chat.message.Message;

import java.util.List;

public interface ChatMessagesView {

	record MonitorChatMessageView (
		ChatMonitorView chatInfo,
		List<MessageMonitorView> messages
	) {
		public static MonitorChatMessageView of(Chat c, List<Message> messages) {
			return new MonitorChatMessageView(
				ChatMonitorView.of(c),
				messages.stream().map(MessageMonitorView::of).toList());
		}
	}

	record SimpleChatMessageView (
		ChatSimpleView chatInfo,
		List<MessageSimpleView> messages
	) {
		public static SimpleChatMessageView of(Chat c, List<Message> messages) {
			return new SimpleChatMessageView(
				ChatSimpleView.of(c),
				messages.stream().map(MessageSimpleView::of).toList());
		}
	}
}
