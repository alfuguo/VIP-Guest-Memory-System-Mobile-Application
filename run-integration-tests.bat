@echo off
echo Running VIP Guest Memory System Integration Tests
echo ================================================

echo.
echo Running Authentication Integration Tests...
mvn test -Dtest="AuthenticationIntegrationTest" -q

echo.
echo Running Guest Management Integration Tests...
mvn test -Dtest="GuestManagementIntegrationTest" -q

echo.
echo Running Visit Management Integration Tests...
mvn test -Dtest="VisitManagementIntegrationTest" -q

echo.
echo Running Notification Integration Tests...
mvn test -Dtest="NotificationIntegrationTest" -q

echo.
echo Running End-to-End Workflow Integration Tests...
mvn test -Dtest="EndToEndWorkflowIntegrationTest" -q

echo.
echo Running Complete Integration Test Suite...
mvn test -Dtest="IntegrationTestSuite" -q

echo.
echo Integration Tests Complete!
echo Check the test reports in target/surefire-reports/ for detailed results.
pause