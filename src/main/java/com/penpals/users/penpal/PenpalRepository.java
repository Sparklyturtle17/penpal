package com.penpals.users.penpal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PenpalRepository extends JpaRepository<Penpal, Long> {

	List<Penpal> findAllPenpalsByParentHelperId(Long parentHelperId);

	Optional<Penpal> findByIdAndParentHelperId(Long id, Long parentHelperId);

	@Query("""
	    select other from Chat c
	      join c.members p
	      join c.members other
	    where p.id = :penpalId
	      and other.id <> :penpalId
	      and c.active = true
	      and p.parentHelper.id = :guardianId
	""")
	Optional<Penpal> findActiveCompanionForGuardian(@Param("penpalId") Long penpalId,
	                                                @Param("guardianId") Long guardianId);

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