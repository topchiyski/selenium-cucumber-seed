@ui @checkout
Feature: Checkout

  Background:
    Given I am logged in as the "standard" user
    And I have added "Sauce Labs Backpack" to the cart

  @smoke
  Scenario: Buy a product as a randomly generated customer
    When I check out with a random customer
    Then the item total should be $29.99
    When I finish the purchase
    Then I should see the order confirmation "Thank you for your order!"

  Scenario: Buy a product with partly specified customer data
    # "random" cells are filled with generated data
    When I check out with the customer:
      | firstName | lastName | postalCode |
      | Ada       | random   | 10115      |
    Then the item total should be $29.99

  Scenario Outline: Customer information is mandatory: <missing>
    When I check out with the customer:
      | firstName   | lastName   | postalCode   |
      | <firstName> | <lastName> | <postalCode> |
    Then I should see the checkout error "<error>"

    Examples:
      | missing     | firstName | lastName | postalCode | error                          |
      | first name  |           | Doe      | 12345      | Error: First Name is required  |
      | last name   | Jane      |          | 12345      | Error: Last Name is required   |
      | postal code | Jane      | Doe      |            | Error: Postal Code is required |
