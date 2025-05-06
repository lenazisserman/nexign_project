package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CrmApiTest {

    private static final String BASE_URI = "http://localhost:8080";
    private static final String MANAGER_LOGIN = "admin";
    private static final String MANAGER_PASSWORD = "admin";
    private static final String TEST_MSISDN = "79998887766";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    void testCreateClient() {
        given()
                .auth().preemptive().basic(MANAGER_LOGIN, MANAGER_PASSWORD)
                .contentType(ContentType.JSON)
                .body("""
                  {
                    "lastName": "Тестов",
                    "firstName": "Тест",
                    "middleName": "Тестович",
                    "msisdn": "%s",
                    "tariffId": 11
                  }
                  """.formatted(TEST_MSISDN))
                .when()
                .post("/v1/clients")
                .then()
                .statusCode(201)
                .body("msisdn", equalTo(TEST_MSISDN))
                .body("firstName", equalTo("Тест"));
    }

    @Test
    void testChangeTariff() {
        given()
                .auth().preemptive().basic(MANAGER_LOGIN, MANAGER_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"tariffId\": 12}")
                .when()
                .patch("/v1/clients/{msisdn}/tariff", TEST_MSISDN)
                .then()
                .statusCode(200)
                .body("msisdn", equalTo(TEST_MSISDN))
                .body("tariffId", equalTo(12));
    }

    @Test
    void testTopUpBalanceAsManager() {
        given()
                .auth().preemptive().basic(MANAGER_LOGIN, MANAGER_PASSWORD)
                .contentType(ContentType.JSON)
                .body("{\"money\": 100.0}")
                .when()
                .post("/v1/clients/{msisdn}/balance", TEST_MSISDN)
                .then()
                .statusCode(200)
                .body("msisdn", equalTo(TEST_MSISDN))
                .body("money", greaterThanOrEqualTo(100.0f));
    }

    @Test
    void testGetFullClientInfo() {
        given()
                .auth().preemptive().basic(MANAGER_LOGIN, MANAGER_PASSWORD)
                .when()
                .get("/v1/clients/{msisdn}", TEST_MSISDN)
                .then()
                .statusCode(200)
                .body("clientInfo.msisdn", equalTo(TEST_MSISDN))
                .body("tariffInfo.id", notNullValue());
    }

    @Test
    void testTopUpBalanceAsClient() {
        given()
                .auth().preemptive().basic(TEST_MSISDN, TEST_MSISDN)
                .contentType(ContentType.JSON)
                .body("{\"money\": 50.0}")
                .when()
                .post("/v1/clients/{msisdn}/balance", TEST_MSISDN)
                .then()
                .statusCode(200)
                .body("msisdn", equalTo(TEST_MSISDN))
                .body("money", greaterThanOrEqualTo(150.0f));
    }
}
