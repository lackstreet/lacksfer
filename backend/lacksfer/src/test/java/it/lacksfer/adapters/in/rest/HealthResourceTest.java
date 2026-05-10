package it.lacksfer.adapters.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HealthResourceTest {
    @Test
    void healthShouldReturnOk() {
        given()
                .when()
                .get("/health")
                .then()
                .statusCode(200)
                .body(is("Lacksfer backend is running"));
    }

}
