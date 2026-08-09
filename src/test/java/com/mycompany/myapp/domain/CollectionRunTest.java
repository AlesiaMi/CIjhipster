package com.mycompany.myapp.domain;

import static com.mycompany.myapp.domain.CollectionRunTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CollectionRunTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CollectionRun.class);
        CollectionRun collectionRun1 = getCollectionRunSample1();
        CollectionRun collectionRun2 = new CollectionRun();
        assertThat(collectionRun1).isNotEqualTo(collectionRun2);

        collectionRun2.setId(collectionRun1.getId());
        assertThat(collectionRun1).isEqualTo(collectionRun2);

        collectionRun2 = getCollectionRunSample2();
        assertThat(collectionRun1).isNotEqualTo(collectionRun2);
    }
}
