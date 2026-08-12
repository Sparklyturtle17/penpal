package com.penpals.chat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

	List<Message> findAllByChatId(Long chatId);

	// what a penpal may see in their chat: all of their OWN messages (any approval
	// state) plus everyone else's only once approved. Blasts have a null author, so
	// they surface via the approved branch.
	@Query("""
	    select m from Message m
	    where m.chat.id = :chatId
	      and (m.penpalAuthor.id = :penpalId or m.approved = true)
	    order by m.id
	""")
	List<Message> findVisibleInChatForPenpal(@Param("chatId") Long chatId, @Param("penpalId") Long penpalId);

	// the penpal is either the penpalAuthor or
	// the other penpal in the same chat as the message is in
	@Query("""
	    select distinct m from Message m
	    	join m.chat c
	    	join c.members member
	    where m.id = :id
	      and member.id = :penpalId
    """)
	Optional<Message> findByIdAndEligiblePenpals(@Param("id") Long id, @Param("penpalId") Long penpalId);

	List<Message> findAllByApprovedNull();

	List<Message> findAllByApprovedFalseOrApprovedNull();

}