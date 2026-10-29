# Visual checks with Applitools Eyes (see README "Visual testing"). Needs the APPLITOOLS_API_KEY environment
# variable; without it these scenarios still run their steps, but the visual checkpoints are skipped.
# The first run with a key creates the baselines; later runs are compared with them.
# Use fixed data here (not "random"): changing content would show up as a visual difference.
@ui @visual
Feature: Visual appearance

  Scenario: Login page looks as expected
    Given I am on the login page
    Then the "Login" page should look as expected

  Scenario: Product list and checkout overview look as expected
    Given I am logged in as the "standard" user
    Then the "Products" page should look as expected
    When I add "Sauce Labs Backpack" to the cart
    And I check out with the customer:
      | firstName | lastName | postalCode |
      | Ada       | Lovelace | 10115      |
    Then the "Checkout overview" page should look as expected
