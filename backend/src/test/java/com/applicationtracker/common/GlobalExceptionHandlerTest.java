package com.applicationtracker.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void badRequestReturnsApiErrorBody() {
        var response = handler.badRequest(new BadRequestException("Invalid request"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid request");
        assertThat(response.getBody().fields()).isEmpty();
    }

    @Test
    void notFoundReturnsApiErrorBody() {
        var response = handler.notFound(new NotFoundException("Resource not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Resource not found");
        assertThat(response.getBody().fields()).isEmpty();
    }

    @Test
    void badCredentialsReturnsUnauthorizedApiErrorBody() {
        var response = handler.badCredentials();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).isEqualTo("Invalid email or password");
        assertThat(response.getBody().fields()).isEmpty();
    }

    @Test
    void apiErrorFactorySetsTimestampAndEmptyFields() {
        ApiError error = ApiError.of(409, "Conflict", "Already exists");

        assertThat(error.timestamp()).isNotNull();
        assertThat(error.status()).isEqualTo(409);
        assertThat(error.error()).isEqualTo("Conflict");
        assertThat(error.message()).isEqualTo("Already exists");
        assertThat(error.fields()).isEmpty();
    }
}
