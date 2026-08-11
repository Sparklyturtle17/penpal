package com.penpals.chat.dto;

import com.penpals.chat.dto.MessageViews.*;
import com.penpals.chat.dto.ChatViews.*;

import java.util.List;

public interface ChatMessagesView {

	record MonitorChatMessageView (
		ChatMonitorView chatInfo,
		List<MessageMonitorView> messages
	) {}

	record SimpleChatMessageView (
		ChatSimpleView chatInfo,
		List<MessageSimpleView> messages
	) {}
}
