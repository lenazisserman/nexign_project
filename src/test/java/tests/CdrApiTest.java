package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CdrApiTest {

    @Test
    public void testGenerateCall() {

        RestAssured.baseURI = "http://localhost:8000";

        // Отправляем POST запрос на эндпоинт /api/calls/generate с параметром count=1
        Response response = given()
                .param("count", 1)
                .when()
                .post("/api/calls/generate")
                .then()
                .log().all()
                //.statusCode(200) // Ожидаем, что ответ будет успешным (HTTP 200)
                .body(equalTo("Звонки сгенерированы и сохранены."))
                .extract().response();
    }
}
