package com.example.e2e.steps;

import com.example.e2e.api.ApiClient;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.Map;

/** Generic REST steps backed by {@link ApiClient}. Paths are relative to {@code api.base.url}. */
public class ApiSteps {

    private final ApiClient api;

    public ApiSteps(ApiClient api) {
        this.api = api;
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequest(String path) {
        api.get(path);
    }

    @When("I send a DELETE request to {string}")
    public void iSendADeleteRequest(String path) {
        api.delete(path);
    }

    /** The two-column table (field | value) becomes the JSON body. */
    @When("I send a POST request to {string} with:")
    public void iSendAPostRequest(String path, Map<String, String> body) {
        api.post(path, body);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expected) {
        Assert.assertEquals(api.lastResponse().statusCode(), expected,
                "HTTP status. Body: " + api.lastResponse().asPrettyString());
    }

    /** {@code jsonPath} uses REST Assured's GPath syntax, e.g. {@code id}, {@code address.city}, {@code [0].title}. */
    @Then("the response field {string} should be {string}")
    public void theResponseFieldShouldBe(String jsonPath, String expected) {
        Object actual = api.lastResponse().jsonPath().get(jsonPath);
        Assert.assertEquals(String.valueOf(actual), expected, "Response field '" + jsonPath + "'");
    }

    @Then("the response should contain {int} items")
    public void theResponseShouldContainItems(int expected) {
        Assert.assertEquals(api.lastResponse().jsonPath().getList("$").size(), expected, "Number of items in response");
    }
}
