package com.mycompany.myapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CiAlertTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static CiAlert getCiAlertSample1() {
        return new CiAlert().id(1L).title("title1").message("message1");
    }

    public static CiAlert getCiAlertSample2() {
        return new CiAlert().id(2L).title("title2").message("message2");
    }

    public static CiAlert getCiAlertRandomSampleGenerator() {
        return new CiAlert().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).message(UUID.randomUUID().toString());
    }
}
