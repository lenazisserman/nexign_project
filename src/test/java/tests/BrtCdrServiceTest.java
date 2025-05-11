package tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BrtCdrServiceTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost:8081";
    }

    static Stream<String> cdrFilesProvider() {
        return Stream.of(
                // Valid files
                "valid_calls.json",

                // Invalid files
                "valid_boundary_values.json",
                "invalid_call_end_before_start.json",
                "invalid_call_over_midnight.json",
                "duplicate_records.json",
                "invalid_extra_columns.json",
                "invalid_call_type.json",
                "invalid_datetime_format.json",
                "invalid_subscriber_number.json",
                "invalid_missing_columns.json",
                "invalid_non_chronological_order.json",
                "only_9_records.json",
                "invalid_self_call.json",
                "invalid_special_characters_in_number.json",
                "invalid_unknown_msisdn.json",
                "wrong_extension.txt",
                "invalid_zero_duration_call.json",
                "empty_file.json"
        );
    }

    @ParameterizedTest
    @MethodSource("cdrFilesProvider")
    void processCdrFiles_ShouldReturnExpectedResult(String filename) {
        try {
            List<CdrTestData> cdrList = loadTestData(filename);


            int expectedStatusCode = getExpectedStatusCodeForFile(filename);

            String response = given()
                    .contentType(ContentType.JSON)
                    .body(cdrList)
                    .when()
                    .post("/api/cdr/process")
                    .then()
                    .statusCode(expectedStatusCode)
                    .extract().asString();

        } catch (RuntimeException e) {
            // Ожидаемое поведение для файлов с невалидными данными
            if (filename.matches("invalid_(datetime_format|extra_columns|)\\.json") || filename.equals("empty_file.json")) {
                assertTrue(e.getMessage().contains("Failed"));
                return;
            }
            throw e; // Пробрасываем другие неожиданные исключения
        }
    }

    private List<CdrTestData> loadTestData(String filename) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            return mapper.readValue(
                    Paths.get("src/test/resources/" + filename).toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, CdrTestData.class)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data: " + filename, e);
        }
    }


    private int getExpectedStatusCodeForFile(String filename) {
        // Логика определения ожидаемого статус-кода
        if (filename.contains("invalid")) {
            return 400;
        }
        return 200;
    }

    static class CdrTestData {
        private String type;
        private String clientMsisdn;
        private String partnerMsisdn;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        // Геттеры и сеттеры
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getClientMsisdn() { return clientMsisdn; }
        public void setClientMsisdn(String clientMsisdn) { this.clientMsisdn = clientMsisdn; }
        public String getPartnerMsisdn() { return partnerMsisdn; }
        public void setPartnerMsisdn(String partnerMsisdn) { this.partnerMsisdn = partnerMsisdn; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}