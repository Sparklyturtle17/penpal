package com.penpals.chat.message.audit;

import com.penpals.chat.message.Message;
import com.penpals.users.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageAuditRepository extends JpaRepository<MessageAudit, Long> {
	@Query(value = """
	    SELECT a.* FROM message_audit a
	    JOIN (
	        SELECT message_id, MAX(archive_time) AS latest_edit
	        FROM message_audit
	        GROUP BY message_id
	    ) g ON g.message_id = a.message_id
	    ORDER BY g.latest_edit DESC, a.message_id DESC, a.archive_time DESC
    """, nativeQuery = true)
	List<MessageAudit> findAllForMessageGroupedView();

	List<MessageAudit> findAllByMessageOrderByArchiveTimeDesc(Message message);

	@Query("""
	    select a from MessageAudit a
	    where a.performedBy = :user or a.editedBy = :user or a.approvedBy = :user
	    order by a.archiveTime desc
	""")
	List<MessageAudit> findAllTouchedBy(@Param("user") AppUser user);
}