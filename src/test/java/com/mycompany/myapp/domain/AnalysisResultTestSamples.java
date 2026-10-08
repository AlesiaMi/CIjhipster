package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AnalysisResultTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static AnalysisResult getAnalysisResultSample1() {
        return new AnalysisResult()
            .id(1L)
            .summary("summary1")
            .topic("topic1")
            .entities("entities1")
            .riskSource("riskSource1")
            .modelName("modelName1")
            .errorMessage("errorMessage1");
    }

    public static AnalysisResult getAnalysisResultSample2() {
        return new AnalysisResult()
            .id(2L)
            .summary("summary2")
            .topic("topic2")
            .entities("entities2")
            .riskSource("riskSource2")
            .modelName("modelName2")
            .errorMessage("errorMessage2");
    }

    public static AnalysisResult getAnalysisResultRandomSampleGenerator() {
        return new AnalysisResult()
            .id(longCount.incrementAndGet())
            .summary(UUID.randomUUID().toString())
            .topic(UUID.randomUUID().toString())
            .entities(UUID.randomUUID().toString())
            .riskSource(UUID.randomUUID().toString())
            .modelName(UUID.randomUUID().toString())
            .errorMessage(UUID.randomUUID().toString());
    }
}
