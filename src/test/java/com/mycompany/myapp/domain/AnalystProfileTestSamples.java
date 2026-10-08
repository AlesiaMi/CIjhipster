package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AnalystProfileTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static AnalystProfile getAnalystProfileSample1() {
        return new AnalystProfile().id(1L).displayName("displayName1").telegramChatId("telegramChatId1");
    }

    public static AnalystProfile getAnalystProfileSample2() {
        return new AnalystProfile().id(2L).displayName("displayName2").telegramChatId("telegramChatId2");
    }

    public static AnalystProfile getAnalystProfileRandomSampleGenerator() {
        return new AnalystProfile()
            .id(longCount.incrementAndGet())
            .displayName(UUID.randomUUID().toString())
            .telegramChatId(UUID.randomUUID().toString());
    }
}
