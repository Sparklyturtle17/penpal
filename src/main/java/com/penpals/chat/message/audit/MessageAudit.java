package com.penpals.chat.message.audit;

import com.penpals.chat.Chat;
import com.penpals.chat.message.Message;
import com.penpals.users.AppUser;
import com.penpals.users.penpal.Penpal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "message_audit")
public class MessageAudit {

	// audit specific rows
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long auditId;

	private Instant archiveTime;

	@ManyToOne
	private AppUser editedBy;



	///////////////////////////////////////////////////////////////

	// message rows copied over
	@ManyToOne
	private Message message;

	@Column(nullable = false)
	private String text;

	@JoinColumn(nullable = false)
	@ManyToOne
	private Penpal penpalAuthor;

	@JoinColumn(nullable = false)
	@ManyToOne
	private AppUser performedBy;

	@Column(nullable = false)
	private Instant createTime;

	@JoinColumn(nullable = false)
	@ManyToOne
	private Chat chat;

	private Boolean approved;

	@ManyToOne
	private AppUser approvedBy;

	private Instant approvedTime;

}