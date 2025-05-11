package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BrtApiTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8081"; // Замените на реальный URL
    }

    //POST /abonents/save

    @Test
    public void testCreateNewAbonentSuccess() {
        String body = """
        {
            "lastName": "Ivanov",
            "firstName": "Ivan",
            "middleName": "Ivanovich",
            "msisdn": "79001112233",
            "tariffId": 1,
            "money": 100.50,
            "date": "2025-05-11T00:00:00Z"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/abonents/save")
                .then()
                .statusCode(201);
    }

    @Test
    public void testCreateAbonentInvalidPhone() {
        String body = """
        {
            "lastName": "Ivanov",
            "firstName": "Ivan",
            "msisdn": "123",
            "tariffId": 1,
            "money": 50,
            "date": "2025-05-11T00:00:00Z"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/abonents/save")
                .then()
                .statusCode(400);
    }

    //GET /abonents/{msisdn}/info

    @Test
    public void testGetAbonentInfoSuccess() {
        given()
                .pathParam("msisdn", "79001112233")
                .when()
                .get("/abonents/{msisdn}/info")
                .then()
                .statusCode(200)
                .body("msisdn", equalTo("79001112233"));
    }

    //PUT /api/abonents/{msisdn}/pay

    @Test
    public void testPayToAbonentSuccess() {
        String body = """
        {
            "msisdn": "79001112233",
            "money": 20.00
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("msisdn", "79001112233")
                .body(body)
                .when()
                .put("/api/abonents/{msisdn}/pay")
                .then()
                .statusCode(200);
    }

    @Test
    public void testPayWithNegativeAmount() {
        String body = """
        {
            "msisdn": "79001112233",
            "money": -10.00
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("msisdn", "79001112233")
                .body(body)
                .when()
                .put("/api/abonents/{msisdn}/pay")
                .then()
                .statusCode(400);
    }

    //PUT /abonents/{msisdn}/changeTariff

    @Test
    public void testChangeTariffSuccess() {
        String body = """
        {
            "tariffId": 2
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("msisdn", "79001112233")
                .body(body)
                .when()
                .put("/abonents/{msisdn}/changeTariff")
                .then()
                .statusCode(200);
    }

    @Test
    public void testChangeTariffInvalidTariff() {
        String body = """
        {
            "tariffId": 9999
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("msisdn", "79001112233")
                .body(body)
                .when()
                .put("/abonents/{msisdn}/changeTariff")
                .then()
                .statusCode(404);
    }
}
