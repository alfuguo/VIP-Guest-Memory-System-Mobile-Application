package com.restaurant.vip;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple unit test to verify test setup is working
 */
class SimpleUnitTest {

    @Test
    void testBasicAssertion() {
        // Arrange
        String expected = "Hello World";
        
        // Act
        String actual = "Hello World";
        
        // Assert
        assertEquals(expected, actual);
        assertTrue(actual.contains("Hello"));
        assertNotNull(actual);
    }

    @Test
    void testMathOperations() {
        // Arrange
        int a = 5;
        int b = 3;
        
        // Act
        int sum = a + b;
        int product = a * b;
        
        // Assert
        assertEquals(8, sum);
        assertEquals(15, product);
        assertTrue(sum > b);
        assertTrue(product > sum);
    }
}