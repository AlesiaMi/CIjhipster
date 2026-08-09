package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.AnalystProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface AnalystProfileRepositoryWithBagRelationships {
    Optional<AnalystProfile> fetchBagRelationships(Optional<AnalystProfile> analystProfile);

    List<AnalystProfile> fetchBagRelationships(List<AnalystProfile> analystProfiles);

    Page<AnalystProfile> fetchBagRelationships(Page<AnalystProfile> analystProfiles);
}
