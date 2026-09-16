package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.ManagerPermission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerPermissionRepository extends JpaRepository<ManagerPermission, Long> {
    List<ManagerPermission> findAllByAssignmentId(Long assignmentId);

    void deleteAllByAssignmentId(Long assignmentId);
}
