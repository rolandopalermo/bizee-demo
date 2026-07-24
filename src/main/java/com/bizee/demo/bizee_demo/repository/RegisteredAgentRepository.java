package com.bizee.demo.bizee_demo.repository;

import com.bizee.demo.bizee_demo.entity.RegisteredAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegisteredAgentRepository extends JpaRepository<RegisteredAgent, Long> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

	List<RegisteredAgent> findAllByOrderByStateAscNameAsc();

	List<RegisteredAgent> findByStateIgnoreCaseOrderByNameAsc(String state);

	/**
	 * Returns agents in {@code state} with remaining capacity ({@code assigned < capacity}),
	 * ordered for equal workload balancing: fewest currently assigned companies first,
	 * then lowest {@code id} as a stable tie-break.
	 * When {@code excludeCompanyId} is set (reassignment), that company is omitted from load counts.
	 */
	@Query("""
			SELECT ra
			   FROM RegisteredAgent ra
			   LEFT JOIN Company c
				   ON c.registeredAgentType = com.bizee.demo.bizee_demo.domain.RegisteredAgentType.REGISTERED_AGENT
				   AND c.registeredAgentId = ra.id
				   AND (:excludeCompanyId IS NULL OR c.id <> :excludeCompanyId)
			   WHERE UPPER(ra.state) = UPPER(:state)
			   GROUP BY ra
			   HAVING COUNT(c.id) < ra.capacity
			   ORDER BY COUNT(c.id) ASC, ra.id ASC
			""")
	List<RegisteredAgent> findAvailableByStateOrderByAssignedCountAsc(
			@Param("state") String state,
			@Param("excludeCompanyId") Long excludeCompanyId
	);

	@Query(
			value = """
                SELECT COALESCE(
                    SUM(
                        GREATEST(
                            ra.capacity - COALESCE(load.assigned_count, 0),
                            0
                        )
                    ),
                    0
                )
                FROM registered_agents ra
                LEFT JOIN (
                    SELECT
                        c.registered_agent_id,
                        COUNT(*) AS assigned_count
                    FROM companies c
                    WHERE c.registered_agent_type = 'REGISTERED_AGENT'
                    GROUP BY c.registered_agent_id
                ) load
                    ON load.registered_agent_id = ra.id
                WHERE UPPER(ra.state) = UPPER(:state)
                """,
			nativeQuery = true
	)
	long sumRemainingCapacityByState(@Param("state") String state);

	@Query("""
			SELECT COALESCE(SUM(ra.capacity), 0)
			FROM RegisteredAgent ra
			WHERE UPPER(ra.state) = UPPER(:state)
			""")
	long sumTotalCapacityByState(@Param("state") String state);
}
