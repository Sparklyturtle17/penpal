package com.penpals.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

	// the penpal must be a member of the chat
	@Query("""
	    select distinct c from Chat c
	    	join c.members member
	    where c.id = :id
	     	and member.id = :penpalId
    """)
	Optional<Chat> findByIdAndEligiblePenpals(@Param("id") Long id, @Param("penpalId") Long penpalId);

	List<Chat> findAllByActiveTrue();
}