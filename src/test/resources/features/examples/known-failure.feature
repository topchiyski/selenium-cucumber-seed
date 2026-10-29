# Excluded from normal runs. It fails on purpose to show what a failure looks like in the reports
# (screenshots, page source, browser console log, URL) and to try out the rerun task:
#   ./gradlew test -Dcucumber.filter.tags=@known-failure -PignoreFailures
#   ./gradlew rerunFailed
@ui @known-failure
Feature: Demonstration of a failing scenario

  Scenario: Products page has an unexpected title
    Given I am logged in as the "standard" user
    Then the current URL should contain "/inventory.html"
    And the page title should be "KNOWN-FAILURE: this title is intentionally wrong"
