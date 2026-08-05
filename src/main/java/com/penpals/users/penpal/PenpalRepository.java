package com.penpals.users.penpal;

import com.penpals.users.ChatMapRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PenpalRepository extends JpaRepository<Penpal, Long> {

	List<Penpal> findAllPenpalsByParentHelperId(Long parentHelperId);

	// whole map (for monitors)
	@Query("""
	    select new com.penpals.users.ChatMapRow(
	        h.id,  concat(h.firstName, ' ', h.lastName),
	        p.id,  concat(p.firstName, ' ', p.lastName),
	        comp.id, concat(comp.firstName, ' ', comp.lastName),
	        ch.id, concat(ch.firstName, ' ', ch.lastName))
	    from Chat c
	      join c.members p
	      join p.parentHelper h
	      join c.members comp
	      join comp.parentHelper ch
	    where comp.id <> p.id
	    order by h.id, p.id
	""")
//	@Query("""
//	    select new com.penpals.users.ChatMapRow(
//	        h.id,  concat(h.firstName, ' ', h.lastName),
//	        p.id,  concat(p.firstName, ' ', p.lastName),
//	        comp.id, concat(comp.firstName, ' ', comp.lastName),
//	        ch.id, concat(ch.firstName, ' ', ch.lastName))
//	    from Chat c
//	      join c.members p
//	      join p.parentHelper h
//	      join c.members comp
//	      join comp.parentHelper ch
//	    where p.id < comp.id
//	    order by h.id, p.id
//	""")
	List<ChatMapRow> findCompleteChatMap();

	// only related users (for parent/helpers penpals)
	@Query("""
	    select new com.penpals.users.ChatMapRow(
	        h.id,  concat(h.firstName, ' ', h.lastName),
	        p.id,  concat(p.firstName, ' ', p.lastName),
	        comp.id, concat(comp.firstName, ' ', comp.lastName),
	        ch.id, concat(ch.firstName, ' ', ch.lastName))
	    from Chat c
	      join c.members p
	      join p.parentHelper h
	      join c.members comp
	      join comp.parentHelper ch
	    where h.id = :parentHelperId
	      and comp.id <> p.id
	    order by p.id
	""")
	List<ChatMapRow> findChatMapByParentHelper(@Param("parentHelperId") Long parentHelperId);

	@Query("select p from Chat c join c.members p where c.id = :chatId")
	List<Penpal> findPenpalsByChatId(@Param("chatId") Long chatId);

	@Query("""
        select other from Chat c
          join c.members p
          join c.members other
        where p.id = :penpalId
          and other.id <> :penpalId
          and c.active = true
    """)
	Optional<Penpal> findActiveChatCompanion(@Param("penpalId") Long penpalId);
}