@ui @login
Feature: Login
  As a shopper
  I want to log in to the shop
  So that I can browse and buy products

  Background:
    Given I am on the login page

  @smoke
  Scenario: Standard user logs in successfully
    When I log in as the "standard" user
    Then I should see the products page
    And the current URL should contain "/inventory.html"

  Scenario Outline: Login is rejected: <reason>
    When I log in with username "<username>" and password "<password>"
    Then I should see the login error "<error>"

    Examples:
      | reason           | username        | password     | error                                                                     |
      | locked out user  | locked_out_user | secret_sauce | Epic sadface: Sorry, this user has been locked out.                       |
      | wrong password   | standard_user   | not-it       | Epic sadface: Username and password do not match any user in this service |
      | missing username |                 | secret_sauce | Epic sadface: Username is required                                        |
      | missing password | standard_user   |              | Epic sadface: Password is required                                        |
