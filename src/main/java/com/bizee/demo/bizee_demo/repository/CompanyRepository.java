package com.bizee.demo.bizee_demo.repository;

import com.bizee.demo.bizee_demo.domain.RegisteredAgentType;
import com.bizee.demo.bizee_demo.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

	Optional<Company> findByIdAndUserId(Long id, Long userId);

	List<Company> findByUserIdOrderByIdAsc(Long userId);

	List<Company> findAllByOrderByIdAsc();

	boolean existsByUserId(Long userId);

	long countByRegisteredAgentTypeAndRegisteredAgentId(RegisteredAgentType registeredAgentType, Long registeredAgentId);

	/**
	 * Companies in {@code state} that use the registered-agent service
	 * ({@code registered_agent_type = registered_agent}).
	 */
	@Query("""
			SELECT COUNT(c)
			FROM Company c
			WHERE UPPER(c.state) = UPPER(:state)
			  AND c.registeredAgentType = com.bizee.demo.bizee_demo.domain.RegisteredAgentType.REGISTERED_AGENT
			""")
	long countServiceAssignedByState(@Param("state") String state);
}
