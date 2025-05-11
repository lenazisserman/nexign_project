package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class HrsApiTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8082"; // Замените на актуальный HRS URL
    }

    //POST /api/call/calculate-cost

    @Test
    public void testCalculateCallCostSuccess() {
        String body = """
        {
            "msisdn": "79001112233",
            "tariffId": 1,
            "callDurationMinutes": 10
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/call/calculate-cost")
                .then()
                .statusCode(200)
                .body("cost", greaterThan(0.0f)); // предполагаем, что возвращается поле cost
    }

    @Test
    public void testCalculateCallCostInvalidDuration() {
        String body = """
        {
            "msisdn": "79001112233",
            "tariffId": 1,
            "callDurationMinutes": -5
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/call/calculate-cost")
                .then()
                .statusCode(400);
    }

    //POST /api/tariff/change

    @Test
    public void testChangeTariffWithDateSuccess() {
        String body = """
        {
            "msisdn": "79001112233",
            "tariffId": 2,
            "changeDate": "2025-05-11T00:00:00Z"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/tariff/change")
                .then()
                .statusCode(200);
    }

    @Test
    public void testChangeTariffWithInvalidMsisdn() {
        String body = """
        {
            "msisdn": "12345",
            "tariffId": 2,
            "changeDate": "2025-05-11T00:00:00Z"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/tariff/change")
                .then()
                .statusCode(400);
    }

    //GET /api/tariff/{tariffId}

    @Test
    public void testGetTariffByIdSuccess() {
        given()
                .pathParam("tariffId", 1)
                .when()
                .get("/api/tariff/{tariffId}")
                .then()
                .statusCode(200)
                .body("tariffId", equalTo(1));
    }

    @Test
    public void testGetTariffByIdNotFound() {
        given()
                .pathParam("tariffId", 9999)
                .when()
                .get("/api/tariff/{tariffId}")
                .then()
                .statusCode(404);
    }
}
