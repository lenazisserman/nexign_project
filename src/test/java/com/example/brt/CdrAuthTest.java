package com.example.brt;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.util.List;

public class CdrAuthTest {

    private final BrtService brtService = new BrtService();

    @Test
    void testBrtProcessCdrFile_withExpectedReactions() throws Exception {
        // Путь к файлу с CDR-записями
        File cdrFile = new File("resources/cdr_test_data.csv");

        // Ожидаемые реакции от BRT для каждой записи
        List<Boolean> expectedResults = List.of(
                true,  // строка 1 — валидный исходящий
                true,  // строка 2 — валидный входящий
                true,  // строка 3 — обычный исходящий
                true,  // строка 4 — обычный входящий
                true,  // строка 5 — межоператор
                true,  // строка 6 — межоператор
                true,  // строка 7 — внутри одного оператора
                true,  // строка 8 — внутри одного оператора
                true,  // строка 9 — через полночь
                true,  // строка 10 — через полночь
                true,  // строка 11 — после полуночи
                true,  // строка 12 — после полуночи
                false, // строка 13 — неверное время окончания
                false, // строка 14 — совпадение номеров
                false, // строка 15 — пропущено время окончания
                false, // строка 16 — пропущено время начала
                false, // строка 17 — невалидный формат времени
                false  // строка 18 — длительность > 24 ч
        );

        // Запускаем обработку файла и получаем реальные результаты
        List<Boolean> actualResults = brtService.processCdrFile(cdrFile);

        // Проверяем, что количество результатов совпадает с ожидаемым
        assertEquals(expectedResults.size(), actualResults.size(),
                "Ожидаемое количество результатов не совпадает с фактическим");

        // Сравниваем каждую реакцию на строку
        for (int i = 0; i < expectedResults.size(); i++) {
            assertEquals(expectedResults.get(i), actualResults.get(i),
                    "Ошибка на строке " + (i + 1));
        }
    }
}
