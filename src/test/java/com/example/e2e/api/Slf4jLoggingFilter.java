package com.example.e2e.api;

import com.example.e2e.logging.Masking;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * Logs every REST Assured call through SLF4J: INFO one line per call (method, URI, status, duration),
 * DEBUG headers and bodies. Sensitive headers (Authorization, Cookie, tokens) are masked and bodies are
 * truncated. Full request/response details are attached to Allure by {@code AllureRestAssured}.
 */
public class Slf4jLoggingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(Slf4jLoggingFilter.class);
    private static final int MAX_BODY_CHARS = 2_000;

    /** Creates the filter; {@code ApiClient} adds one to its request specification. */
    public Slf4jLoggingFilter() {
    }

    /**
     * Logs the request at DEBUG, passes it down the filter chain and logs the outcome.
     *
     * @param request the request about to be sent
     * @param responseSpec the response specification of the request
     * @param ctx the rest of the filter chain
     * @return the response returned by the chain
     */
    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification responseSpec, FilterContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("--> {} {}\n    headers: {}\n    body: {}", request.getMethod(), request.getURI(),
                    headers(request.getHeaders().asList()), Masking.truncate(String.valueOf(request.<Object>getBody()), MAX_BODY_CHARS));
        }
        long start = System.nanoTime();
        Response response;
        try {
            response = ctx.next(request, responseSpec);
        } catch (RuntimeException e) {
            LOG.error("{} {} failed after {} ms: {}", request.getMethod(), request.getURI(), elapsedMillis(start), e.toString());
            throw e;
        }
        long millis = elapsedMillis(start);
        int status = response.getStatusCode();
        if (status >= 500) {
            LOG.warn("{} {} -> {} ({} ms)", request.getMethod(), request.getURI(), status, millis);
        } else {
            LOG.info("{} {} -> {} ({} ms)", request.getMethod(), request.getURI(), status, millis);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("<-- {} {}\n    headers: {}\n    body: {}", status, request.getURI(),
                    headers(response.getHeaders().asList()), Masking.truncate(response.asString(), MAX_BODY_CHARS));
        }
        return response;
    }

    private static String headers(java.util.List<Header> headers) {
        return headers.stream()
                .map(h -> h.getName() + "=" + Masking.mask(h.getName(), h.getValue()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
