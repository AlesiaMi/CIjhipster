package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CompetitorTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Competitor getCompetitorSample1() {
        return new Competitor()
            .id(1L)
            .competitorName("competitorName1")
            .websiteUrl("websiteUrl1")
            .industry("industry1")
            .description("description1");
    }

    public static Competitor getCompetitorSample2() {
        return new Competitor()
            .id(2L)
            .competitorName("competitorName2")
            .websiteUrl("websiteUrl2")
            .industry("industry2")
            .description("description2");
    }

    public static Competitor getCompetitorRandomSampleGenerator() {
        return new Competitor()
            .id(longCount.incrementAndGet())
            .competitorName(UUID.randomUUID().toString())
            .websiteUrl(UUID.randomUUID().toString())
            .industry(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
