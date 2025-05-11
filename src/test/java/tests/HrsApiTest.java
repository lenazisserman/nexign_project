package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.stream.Stream;

public class HrsApiTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8082";
    }

    //POST /api/call/calculate-cost

    @Test
    public void testCalculateCallCostSuccess() {
        String body = """
        {
            "msisdn": "79001112233",
            "tariffId": 11,
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
                .body("cost", greaterThan(0.0f));
    }

    @Test
    public void testCalculateCallCostInvalidDuration() {
        String body = """
        {
            "msisdn": "79001112233",
            "tariffId": 11,
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
            "tariffId": 12,
            "changeDate": "2025-05-11T00:00:00"
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
            "tariffId": 12,
            "changeDate": "2025-05-11T00:00:00"
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
    public void testTariff12_WithMonthlyFeeAndIncludedMinutes() {
        given()
                .pathParam("tariffId", 12)
                .when()
                .get("/api/tariff/{tariffId}")
                .then()
                .statusCode(200)
                .body("tariff.monthlyFee", equalTo(100))
                .body("tariff.includedMinutes", equalTo(50))
                .body("tariff.overlimitTariffId", equalTo(11))
                .body("tariff.currency", equalTo("у.е."))
                .body("tariff.callTypes.incoming", equalTo(true))
                .body("tariff.callTypes.outgoing.external", equalTo(true))
                .body("tariff.callTypes.outgoing.internal", equalTo(true))
                .body("tariff.description", not(emptyString()));
    }

    @Test
    public void testTariff11_WithPerMinuteRates() {
        given()
                .pathParam("tariffId", 11)
                .when()
                .get("/api/tariff/{tariffId}")
                .then()
                .statusCode(200)
                .body("tariff.incomingCalls.costPerMinute", equalTo(0))
                .body("tariff.incomingCalls.currency", equalTo("у.е."))
                .body("tariff.outgoingCalls.external.costPerMinute", equalTo(2.5f))
                .body("tariff.outgoingCalls.external.currency", equalTo("у.е."))
                .body("tariff.outgoingCalls.internal.costPerMinute", equalTo(1.5f))
                .body("tariff.outgoingCalls.internal.currency", equalTo("у.е."))
                .body("tariff.description", not(emptyString()));
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
