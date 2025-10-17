package com.restaurant.vip.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Integration test suite that runs all integration tests in a specific order.
 * This ensures that tests are executed systematically and provides a comprehensive
 * validation of the entire VIP Guest Memory System.
 */
@Suite
@SuiteDisplayName("VIP Guest Memory System - Integration Test Suite")
@SelectClasses({
    AuthenticationIntegrationTest.class,
    GuestManagementIntegrationTest.class,
    VisitManagementIntegrationTest.class,
    NotificationIntegrationTest.class,
    EndToEndWorkflowIntegrationTest.class
})
public class IntegrationTestSuite {
    // This class serves as a test suite runner
    // Individual test classes contain the actual test methods
}