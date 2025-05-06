package nexign;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CDRGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final Random random = new Random();
    private static final int RECORDS_PER_FILE = 10;

    public static void main(String[] args) {
        generatePositiveTestCDR("positive_test.cdr");
        generateNegativeTestCDR("negative_test.cdr");
    }

    // Генерация позитивных тестов (валидные CDR)
    public static void generatePositiveTestCDR(String filename) {
        List<String> cdrRecords = new ArrayList<>();

        // Генерация случайных валидных CDR
        for (int i = 0; i < RECORDS_PER_FILE; i++) {
            cdrRecords.add(generateValidCDR());
        }

        writeToFile(filename, cdrRecords);
    }

    // Генерация негативных тестов (невалидные CDR)
    public static void generateNegativeTestCDR(String filename) {
        List<String> cdrRecords = new ArrayList<>();

        // Генерация 10 невалидных CDR разных типов
        for (int i = 0; i < RECORDS_PER_FILE; i++) {
            cdrRecords.add(generateInvalidCDR(i % 6)); // 6 типов ошибок
        }

        writeToFile(filename, cdrRecords);
    }

    private static String generateValidCDR() {
        String callType = random.nextBoolean() ? "01" : "02";
        String subscriberNumber = generateRandomPhoneNumber();
        String partnerNumber = generateRandomPhoneNumber();

        // Гарантируем, что номера не совпадают
        while (partnerNumber.equals(subscriberNumber)) {
            partnerNumber = generateRandomPhoneNumber();
        }

        LocalDateTime startTime = generateRandomDateTime();
        LocalDateTime endTime = startTime.plusSeconds(ThreadLocalRandom.current().nextInt(10, 3600));

        return String.format("%s,%s,%s,%s,%s",
                callType,
                subscriberNumber,
                partnerNumber,
                startTime.format(DATE_FORMATTER),
                endTime.format(DATE_FORMATTER));
    }

    private static String generateInvalidCDR(int errorType) {
        String callType = random.nextBoolean() ? "01" : "02";
        String subscriberNumber = generateRandomPhoneNumber();
        String partnerNumber = generateRandomPhoneNumber();
        LocalDateTime startTime = generateRandomDateTime();
        LocalDateTime endTime = startTime.plusSeconds(ThreadLocalRandom.current().nextInt(10, 3600));

        switch (errorType) {
            case 0: // Неверное время окончания звонка
                return String.format("%s,%s,%s,%s,%s",
                        callType, subscriberNumber, partnerNumber,
                        endTime.format(DATE_FORMATTER), startTime.format(DATE_FORMATTER));

            case 1: // Совпадение номеров абонентов
                return String.format("%s,%s,%s,%s,%s",
                        callType, subscriberNumber, subscriberNumber,
                        startTime.format(DATE_FORMATTER), endTime.format(DATE_FORMATTER));

            case 2: // Пропущенные обязательные данные (время окончания)
                return String.format("%s,%s,%s,%s,",
                        callType, subscriberNumber, partnerNumber,
                        startTime.format(DATE_FORMATTER));

            case 3: // Пропущенное время начала звонка
                return String.format("%s,%s,%s,,%s",
                        callType, subscriberNumber, partnerNumber,
                        endTime.format(DATE_FORMATTER));

            case 4: // Невалидный формат времени
                return String.format("%s,%s,%s,%s,%s",
                        callType, subscriberNumber, partnerNumber,
                        "2025-02-10T10:12", endTime.format(DATE_FORMATTER));

            case 5: // Период звонка более 24 часов
                return String.format("%s,%s,%s,%s,%s",
                        callType, subscriberNumber, partnerNumber,
                        startTime.format(DATE_FORMATTER), startTime.plusHours(25).format(DATE_FORMATTER));

            default:
                return generateValidCDR();
        }
    }

    private static String generateRandomPhoneNumber() {
        return "79" + String.format("%09d", random.nextInt(1_000_000_000));
    }

    private static LocalDateTime generateRandomDateTime() {
        int year = 2025;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);

        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    private static void writeToFile(String filename, List<String> records) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String record : records) {
                writer.write(record);
                writer.newLine();
            }
            System.out.println("Файл " + filename + " успешно создан с " + records.size() + " записями.");
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
}