package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CollectionRunTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static CollectionRun getCollectionRunSample1() {
        return new CollectionRun().id(1L).foundCount(1).processedCount(1).errorMessage("errorMessage1");
    }

    public static CollectionRun getCollectionRunSample2() {
        return new CollectionRun().id(2L).foundCount(2).processedCount(2).errorMessage("errorMessage2");
    }

    public static CollectionRun getCollectionRunRandomSampleGenerator() {
        return new CollectionRun()
            .id(longCount.incrementAndGet())
            .foundCount(intCount.incrementAndGet())
            .processedCount(intCount.incrementAndGet())
            .errorMessage(UUID.randomUUID().toString());
    }
}
