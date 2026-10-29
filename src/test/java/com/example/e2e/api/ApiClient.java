package com.example.e2e.api;

import com.example.e2e.config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Thin REST Assured wrapper for calling backend APIs from steps, e.g. to create test data before a
 * UI flow or to verify state after it. Every request/response is attached to the Allure report and
 * logged through SLF4J ({@link Slf4jLoggingFilter}).
 * <p>
 * Injected per scenario by PicoContainer; the last response is kept so "Then" steps can assert on it.
 */
public class ApiClient {

    private final RequestSpecification baseSpec;
    private Response lastResponse;

    /**
     * Creates a client for the configured API.
     *
     * @param config source of {@code api.base.url}
     */
    public ApiClient(Config config) {
        this.baseSpec = new RequestSpecBuilder()
                .setBaseUri(config.apiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new Slf4jLoggingFilter())
                .build();
    }

    /**
     * A request pre-configured with the base URI, JSON headers and reporting. Add auth headers here if needed.
     *
     * @return a new request specification
     */
    public RequestSpecification request() {
        return RestAssured.given().spec(baseSpec);
    }

    /**
     * Sends a GET request and remembers the response for {@link #lastResponse()}.
     *
     * @param path path relative to {@code api.base.url}, e.g. {@code /posts/1}
     * @return the response
     */
    public Response get(String path) {
        return remember(request().get(path));
    }

    /**
     * Sends a POST request with a JSON body and remembers the response.
     *
     * @param path path relative to {@code api.base.url}
     * @param body object serialized to JSON by Jackson, e.g. a {@code Map} or a POJO
     * @return the response
     */
    public Response post(String path, Object body) {
        return remember(request().body(body).post(path));
    }

    /**
     * Sends a PUT request with a JSON body and remembers the response.
     *
     * @param path path relative to {@code api.base.url}
     * @param body object serialized to JSON by Jackson
     * @return the response
     */
    public Response put(String path, Object body) {
        return remember(request().body(body).put(path));
    }

    /**
     * Sends a DELETE request and remembers the response.
     *
     * @param path path relative to {@code api.base.url}
     * @return the response
     */
    public Response delete(String path) {
        return remember(request().delete(path));
    }

    /**
     * The response of the most recent request sent in this scenario.
     *
     * @return the last response
     * @throws IllegalStateException if no request has been sent yet
     */
    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("No API request has been sent in this scenario yet");
        }
        return lastResponse;
    }

    private Response remember(Response response) {
        lastResponse = response;
        return response;
    }
}
