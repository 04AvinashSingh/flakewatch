package com.sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ STABLE TESTS — These always pass.
 * Flakewatch should NOT quarantine these.
 */
class CalculatorTest {

    private final Calculator calc = new Calculator();

    @Test
    @DisplayName("Addition works correctly")
    void testAdd() {
        assertEquals(5, calc.add(2, 3));
        assertEquals(0, calc.add(-1, 1));
        assertEquals(-5, calc.add(-2, -3));
    }

    @Test
    @DisplayName("Subtraction works correctly")
    void testSubtract() {
        assertEquals(1, calc.subtract(3, 2));
        assertEquals(-2, calc.subtract(-1, 1));
    }

    @Test
    @DisplayName("Multiplication works correctly")
    void testMultiply() {
        assertEquals(6, calc.multiply(2, 3));
        assertEquals(0, calc.multiply(5, 0));
    }

    @Test
    @DisplayName("Division works correctly")
    void testDivide() {
        assertEquals(2.0, calc.divide(6, 3));
        assertEquals(2.5, calc.divide(5, 2));
    }

    @Test
    @DisplayName("Division by zero throws exception")
    void testDivideByZero() {
        assertThrows(ArithmeticException.class, () -> calc.divide(1, 0));
    }
}
