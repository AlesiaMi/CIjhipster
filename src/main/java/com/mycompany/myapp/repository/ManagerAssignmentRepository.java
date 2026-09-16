package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.ManagerAssignment;
import com.mycompany.myapp.domain.enumeration.ManagerPermissionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerAssignmentRepository extends JpaRepository<ManagerAssignment, Long> {
    Optional<ManagerAssignment> findOneByManagerIdAndClientUserId(Long managerId, Long clientUserId);

    Optional<ManagerAssignment> findOneByManagerIdAndClientUserIdAndActiveTrue(Long managerId, Long clientUserId);

    boolean existsByManagerIdAndClientUserIdAndActiveTrue(Long managerId, Long clientUserId);

    @Query(
        """
        select distinct assignment
        from ManagerAssignment assignment
        join fetch assignment.manager
        join fetch assignment.clientUser
        left join fetch assignment.permissions
        order by assignment.id
        """
    )
    List<ManagerAssignment> findAllWithRelationships();

    @Query(
        """
        select distinct assignment
        from ManagerAssignment assignment
        join fetch assignment.manager
        join fetch assignment.clientUser
        left join fetch assignment.permissions
        where assignment.id = :id
        """
    )
    Optional<ManagerAssignment> findOneWithRelationships(@Param("id") Long id);

    @Query(
        """
        select count(assignment) > 0
        from ManagerAssignment assignment
        join assignment.permissions permission
        where assignment.manager.id = :managerId
          and assignment.clientUser.id = :clientUserId
          and assignment.active = true
          and permission.permission = :permission
        """
    )
    boolean hasPermission(
        @Param("managerId") Long managerId,
        @Param("clientUserId") Long clientUserId,
        @Param("permission") ManagerPermissionType permission
    );

    @Query(
        """
        select distinct assignment.clientUser.id
        from ManagerAssignment assignment
        join assignment.permissions permission
        where assignment.manager.id = :managerId
        and assignment.active = true
        and permission.permission = :permission
        order by assignment.clientUser.id
        """
    )
    List<Long> findClientUserIdsByManagerIdAndPermission(
        @Param("managerId") Long managerId,
        @Param("permission") ManagerPermissionType permission
    );

    @Query(
        """
        select distinct assignment
        from ManagerAssignment assignment
        join fetch assignment.clientUser
        left join fetch assignment.permissions
        where assignment.manager.id = :managerId
        and assignment.active = true
        order by assignment.clientUser.login
        """
    )
    List<ManagerAssignment> findAllActiveByManagerIdWithRelationships(@Param("managerId") Long managerId);
}
