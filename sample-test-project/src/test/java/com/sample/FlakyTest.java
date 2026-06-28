package com.sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 🪲 FLAKY TESTS — These randomly pass or fail!
 * 
 * These simulate real-world flaky tests caused by:
 * - Race conditions (timing-dependent)
 * - Random data
 * - External dependency flakiness
 * 
 * When you run these multiple times and send results to Flakewatch,
 * it will detect the PASS→FAIL→PASS pattern and quarantine them.
 */
class FlakyTest {

    /**
     * Simulates a race condition — depends on timing.
     * Sometimes passes, sometimes fails. Classic flake.
     */
    @Test
    @DisplayName("Flaky: Race condition in async processing")
    void testAsyncProcessing() {
        // Simulate: "Does a background task complete within 50ms?"
        // Sometimes it does, sometimes it doesn't — flaky!
        long startTime = System.nanoTime();
        
        // Do some "work" that takes a variable amount of time
        double result = 0;
        int iterations = (int) (Math.random() * 50000) + 10000;
        for (int i = 0; i < iterations; i++) {
            result += Math.sin(i) * Math.cos(i);
        }
        
        long elapsed = System.nanoTime() - startTime;
        long elapsedMs = elapsed / 1_000_000;
        
        // This threshold makes it sometimes pass, sometimes fail
        assertTrue(elapsedMs < 2, 
            "Async processing took too long: " + elapsedMs + "ms (expected < 2ms)");
    }

    /**
     * Simulates a test that depends on random data.
     * ~50% chance of passing.
     */
    @Test
    @DisplayName("Flaky: Random data dependency")
    void testRandomDataValidation() {
        // Simulate: "Generate a random user ID and check if it's even"
        int userId = (int) (Math.random() * 1000);
        
        assertTrue(userId % 2 == 0, 
            "Generated userId " + userId + " is not even — data-dependent flake!");
    }

    /**
     * Simulates a test that depends on system time.
     * Fails when the current second is odd.
     */
    @Test
    @DisplayName("Flaky: Time-dependent assertion")
    void testTimeDependentLogic() {
        // Simulate: "This feature only works at certain times"
        long currentSecond = System.currentTimeMillis() / 1000;
        
        assertTrue(currentSecond % 2 == 0, 
            "Time-dependent failure at second " + currentSecond + " — classic flake!");
    }
}
