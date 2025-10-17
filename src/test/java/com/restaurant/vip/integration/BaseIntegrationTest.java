package com.restaurant.vip.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.vip.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests that provides common configuration
 * and utilities for testing REST endpoints with database integration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Common setup for all integration tests
        setupTestData();
    }

    /**
     * Override this method in subclasses to set up specific test data
     */
    protected void setupTestData() {
        // Default implementation - can be overridden
    }

    /**
     * Helper method to convert objects to JSON strings
     */
    protected String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Helper method to convert JSON strings to objects
     */
    protected <T> T fromJsonString(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}