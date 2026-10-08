package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.AnalystProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class AnalystProfileRepositoryWithBagRelationshipsImpl implements AnalystProfileRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String ANALYSTPROFILES_PARAMETER = "analystProfiles";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<AnalystProfile> fetchBagRelationships(Optional<AnalystProfile> analystProfile) {
        return analystProfile.map(this::fetchCompetitorses);
    }

    @Override
    public Page<AnalystProfile> fetchBagRelationships(Page<AnalystProfile> analystProfiles) {
        return new PageImpl<>(
            fetchBagRelationships(analystProfiles.getContent()),
            analystProfiles.getPageable(),
            analystProfiles.getTotalElements()
        );
    }

    @Override
    public List<AnalystProfile> fetchBagRelationships(List<AnalystProfile> analystProfiles) {
        return Optional.of(analystProfiles).map(this::fetchCompetitorses).orElse(List.of());
    }

    AnalystProfile fetchCompetitorses(AnalystProfile result) {
        return entityManager
            .createQuery(
                "select analystProfile from AnalystProfile analystProfile left join fetch analystProfile.competitorses where analystProfile.id = :id",
                AnalystProfile.class
            )
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<AnalystProfile> fetchCompetitorses(List<AnalystProfile> analystProfiles) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, analystProfiles.size()).forEach(index -> order.put(analystProfiles.get(index).getId(), index));
        List<AnalystProfile> result = entityManager
            .createQuery(
                "select analystProfile from AnalystProfile analystProfile left join fetch analystProfile.competitorses where analystProfile in :analystProfiles",
                AnalystProfile.class
            )
            .setParameter(ANALYSTPROFILES_PARAMETER, analystProfiles)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
