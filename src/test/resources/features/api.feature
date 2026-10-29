# @api scenarios don't start a browser (see Hooks#startBrowser), so they are fast.
@api
Feature: Posts API
  Example REST checks against https://jsonplaceholder.typicode.com (api.base.url).
  Use the same steps to set up or verify data around UI scenarios.

  @smoke
  Scenario: Fetch a single post
    When I send a GET request to "/posts/1"
    Then the response status should be 200
    And the response field "id" should be "1"
    And the response field "userId" should be "1"

  Scenario: List the comments of a post
    When I send a GET request to "/posts/1/comments"
    Then the response status should be 200
    And the response should contain 5 items

  Scenario: Create a post
    When I send a POST request to "/posts" with:
      | title  | Hello from the seed project |
      | body   | Created by an e2e test      |
      | userId | 1                           |
    Then the response status should be 201
    And the response field "title" should be "Hello from the seed project"
