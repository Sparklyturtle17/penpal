package com.penpals.users.penpal;

import com.penpals.users.ChatMapRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PenpalRepository extends JpaRepository<Penpal, Long> {

	List<Penpal> findAllPenpalsByParentHelperId(Long parentHelperId);

	// each active chat once, as [penpalOne, penpalTwo] with penpalOne.id < penpalTwo.id
	@Query("""
	    select p, other from Chat c
	      join c.members p
	      join c.members other
	    where p.id < other.id
	      and c.active = true
	    order by p.id
	""")
	List<Object[]> findActiveChatPairs();

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