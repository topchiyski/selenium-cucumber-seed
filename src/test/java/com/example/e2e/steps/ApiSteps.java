package com.example.e2e.steps;

import com.example.e2e.api.ApiClient;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.Map;

/** Generic REST steps backed by {@link ApiClient}. Paths are relative to {@code api.base.url}. */
public class ApiSteps {

    private final ApiClient api;

    /**
     * Creates the steps for one scenario.
     *
     * @param api the scenario's API client
     */
    public ApiSteps(ApiClient api) {
        this.api = api;
    }

    /**
     * {@code When I send a GET request to "/posts/1"}
     *
     * @param path path relative to {@code api.base.url}
     */
    @When("I send a GET request to {string}")
    public void iSendAGetRequest(String path) {
        api.get(path);
    }

    /**
     * {@code When I send a DELETE request to "/posts/1"}
     *
     * @param path path relative to {@code api.base.url}
     */
    @When("I send a DELETE request to {string}")
    public void iSendADeleteRequest(String path) {
        api.delete(path);
    }

    /**
     * The two-column table (field | value) becomes the JSON body.
     *
     * @param path path relative to {@code api.base.url}
     * @param body the table as field/value pairs
     */
    @When("I send a POST request to {string} with:")
    public void iSendAPostRequest(String path, Map<String, String> body) {
        api.post(path, body);
    }

    /**
     * {@code Then the response status should be 200}
     *
     * @param expected the expected HTTP status
     */
    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expected) {
        Assert.assertEquals(api.lastResponse().statusCode(), expected,
                "HTTP status. Body: " + api.lastResponse().asPrettyString());
    }

    /**
     * {@code jsonPath} uses REST Assured's GPath syntax, e.g. {@code id}, {@code address.city}, {@code [0].title}.
     *
     * @param jsonPath the field
     * @param expected the expected value, compared as text
     */
    @Then("the response field {string} should be {string}")
    public void theResponseFieldShouldBe(String jsonPath, String expected) {
        Object actual = api.lastResponse().jsonPath().get(jsonPath);
        Assert.assertEquals(String.valueOf(actual), expected, "Response field '" + jsonPath + "'");
    }

    /**
     * {@code Then the response should contain 5 items}: the response is a JSON array of this size.
     *
     * @param expected the expected number of items
     */
    @Then("the response should contain {int} items")
    public void theResponseShouldContainItems(int expected) {
        Assert.assertEquals(api.lastResponse().jsonPath().getList("$").size(), expected, "Number of items in response");
    }
}
