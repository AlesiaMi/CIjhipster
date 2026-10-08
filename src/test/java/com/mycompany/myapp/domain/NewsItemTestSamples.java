package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class NewsItemTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static NewsItem getNewsItemSample1() {
        return new NewsItem().id(1L).externalId("externalId1").title("title1").url("url1").originalText("originalText1");
    }

    public static NewsItem getNewsItemSample2() {
        return new NewsItem().id(2L).externalId("externalId2").title("title2").url("url2").originalText("originalText2");
    }

    public static NewsItem getNewsItemRandomSampleGenerator() {
        return new NewsItem()
            .id(longCount.incrementAndGet())
            .externalId(UUID.randomUUID().toString())
            .title(UUID.randomUUID().toString())
            .url(UUID.randomUUID().toString())
            .originalText(UUID.randomUUID().toString());
    }
}
