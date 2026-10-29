@ui @cart
Feature: Product catalogue and shopping cart

  Background:
    Given I am logged in as the "standard" user

  @smoke
  Scenario: Add several products to the cart
    When I add the following products to the cart:
      | Sauce Labs Backpack   |
      | Sauce Labs Bike Light |
    Then the cart badge should show 2
    When I open the cart
    Then the cart should contain:
      | Sauce Labs Backpack   |
      | Sauce Labs Bike Light |

  Scenario: Sort products by price
    When I sort the products by price (low to high)
    Then the product prices should be in ascending order
    When I sort the products by price (high to low)
    Then the product prices should be in descending order

  Scenario: Sort products by name
    When I sort the products by name (Z to A)
    Then the product names should be in descending order

  # Uses soft assertions: all checks run and every failure is reported at the end.
  Scenario: Product catalogue is complete
    Then the product catalogue should be complete
