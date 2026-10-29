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

    public ApiClient(Config config) {
        this.baseSpec = new RequestSpecBuilder()
                .setBaseUri(config.apiBaseUrl())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .addFilter(new Slf4jLoggingFilter())
                .build();
    }

    /** A request pre-configured with the base URI, JSON headers and reporting. Add auth headers here if needed. */
    public RequestSpecification request() {
        return RestAssured.given().spec(baseSpec);
    }

    public Response get(String path) {
        return remember(request().get(path));
    }

    public Response post(String path, Object body) {
        return remember(request().body(body).post(path));
    }

    public Response put(String path, Object body) {
        return remember(request().body(body).put(path));
    }

    public Response delete(String path) {
        return remember(request().delete(path));
    }

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
