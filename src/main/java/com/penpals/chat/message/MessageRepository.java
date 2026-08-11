package com.penpals.chat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

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

}