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
        // Устанавливаем базовый URL для всех тестов
        RestAssured.baseURI = "http://localhost:8080"; // Замените на реальный URL вашего сервиса
    }

    // Тест на расчет стоимости звонка
    @Test
    public void testCalculateCallCost() {
        // Тело запроса
        String requestBody = "{ \"msisdnClient\": \"1234567890\", \"callDuration\": 5, \"tariffId\": 12 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/calculate-call-cost")
                .then()
                .statusCode(200)
                .body("money", notNullValue());  // Проверяем, что в ответе есть поле "money"
    }

    // Тест на продление тарифа
    @Test
    public void testExtendTariff() {
        // Тело запроса
        String requestBody = "{ \"msisdn\": \"1234567890\", \"tariffId\": 12 }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/extend-tariff")
                .then()
                .statusCode(200)
                .body("message", equalTo("Tariff extended successfully"));
    }

    // Тест на смену тарифа
    @Test
    public void testChangeTariff() {
        // Тело запроса
        String requestBody = "{ \"msisdn\": \"1234567890\", \"newTariffId\": 13, \"changeDate\": \"2025-05-06\" }";

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/change-tariff")
                .then()
                .statusCode(200)
                .body("message", equalTo("Tariff changed successfully"));
    }

    // Тест на получение параметров тарифа
    @Test
    public void testGetTariffParameters() {
        int tariffId = 12; // ID тарифа для теста

        given()
                .when()
                .get("/get-tariff-parameters/" + tariffId)
                .then()
                .statusCode(200)
                .body("tariff.id", equalTo(tariffId))
                .body("tariff.name", equalTo("Помесячный"));
    }

    // Тест на проверку доступности сервиса
    @Test
    public void testCheckServiceStatus() {
        given()
                .when()
                .get("/check-service-status")
                .then()
                .statusCode(200)
                .body("status", equalTo("Service is available"));
    }
}
