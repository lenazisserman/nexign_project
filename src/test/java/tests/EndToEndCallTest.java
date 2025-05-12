package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndCallTest {

    private static final String MSISDN = "79009998877";
    private static final String PARTNER_MSISDN = "79001112233";
    private static final int TARIFF_ID = 11;

    private static BigDecimal balanceBefore;
    private static LocalDateTime startTime;
    private static LocalDateTime endTime;

    // BRT DB connection details from docker-compose
    private static final String BRT_DB_URL = "jdbc:postgresql://localhost:5432/brt-database";
    private static final String BRT_DB_USER = "brt_admin";
    private static final String BRT_DB_PASSWORD = "brt_admin";

    // HRS DB connection details from docker-compose
    private static final String HRS_DB_URL = "jdbc:postgresql://localhost:5433/hrs-database";
    private static final String HRS_DB_USER = "hrs_admin";
    private static final String HRS_DB_PASSWORD = "hrs_admin";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8081"; // BRT service
    }


    @Test
    @Order(1)
    public void createAbonent() {
        String body = """
        {
            "lastName": "Тестов",
            "firstName": "Абонент",
            "middleName": "Тестович",
            "msisdn": "%s",
            "tariffId": %d,
            "money": 100,
            "date": "%s"
        }
        """.formatted(MSISDN, TARIFF_ID, LocalDateTime.now());

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/abonents/save")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(2)
    public void getBalanceBeforeCall() throws SQLException {
        balanceBefore = getBalanceFromBrtDb(MSISDN);
        System.out.println("Баланс ДО звонка: " + balanceBefore);
        assertNotNull(balanceBefore);
    }

    @Test
    @Order(3)
    public void sendCallCdr() {
        startTime = LocalDateTime.now().minusMinutes(2);
        endTime = LocalDateTime.now();

        Map<String, Object> cdr = Map.of(
                "type", "01",
                "clientMsisdn", MSISDN,
                "partnerMsisdn", PARTNER_MSISDN,
                "startTime", startTime.toString(),
                "endTime", endTime.toString()
        );

        given()
                .contentType(ContentType.JSON)
                .body(new Map[]{cdr})
                .when()
                .post("/api/cdr/process")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    public void getBalanceAfterCall() throws SQLException {

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        BigDecimal balanceAfter = getBalanceFromBrtDb(MSISDN);
        System.out.println("Баланс ПОСЛЕ звонка: " + balanceAfter);
        assertNotNull(balanceAfter);
        assertTrue(balanceAfter.compareTo(balanceBefore) < 0, "Баланс должен уменьшиться");
    }

    @Test
    @Order(5)
    public void checkCallInHrs() throws SQLException {
        int duration = getUsedMinutesFromHrsDb(MSISDN);
        System.out.println("Минут в HRS: " + duration);
        assertTrue(duration >= 2, "HRS должен зафиксировать хотя бы 2 минуты");
    }



    private BigDecimal getBalanceFromBrtDb(String msisdn) throws SQLException {
        try (Connection conn = DriverManager.getConnection(BRT_DB_URL, BRT_DB_USER, BRT_DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT money FROM clients WHERE msisdn = ?");
            stmt.setString(1, msisdn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("money");
            } else {
                throw new IllegalArgumentException("Абонент не найден в BRT");
            }
        }
    }

    private int getUsedMinutesFromHrsDb(String msisdn) throws SQLException {
        try (Connection conn = DriverManager.getConnection(HRS_DB_URL, HRS_DB_USER, HRS_DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("""
                SELECT SUM(duration_minutes) AS total
                FROM calls
                WHERE msisdn = ?
            """);
            stmt.setString(1, msisdn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            } else {
                return 0;
            }
        }
    }
}
