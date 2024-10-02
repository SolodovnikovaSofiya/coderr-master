package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MainTest {

    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @Before
    public void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    private String getOutput() {
        return testOut.toString();
    }

    @After
    public void restoreSystemInputOutput() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    @Test
    public void testEncryptFile() throws IOException {
        // Подготовка входных данных
        String input = "C:\\Users\\217551\\Desktop";
        provideInput(input);

        // Создание тестового файла
        String testFilePath = "testFile.txt";
        Files.write(Paths.get(testFilePath), "Приветкек, мир!".getBytes());

        // Вызов метода main
        Main.main(new String[]{});

        // Проверка вывода
        String expectedOutput = "Выберите режим работы:\n" +
                "1. Шифрование\n" +
                "2. Расшифровка\n" +
                "3. Расшифровка методом brute force (опционально)\n" +
                "4. Расшифровка методом статистического анализа (опционально)\n" +
                "Введите путь к файлу:\n" +
                "Вы ввели корректный путь к файлу: testFile.txt\n" +
                "Введите ключ (целое число):\n" +
                "Успех\n";

        assertEquals(expectedOutput, getOutput());

        // Проверка зашифрованного файла
        String encryptedFilePath = "testFile.txt.encrypted";
        String encryptedContent = new String(Files.readAllBytes(Paths.get(encryptedFilePath)));
        assertEquals("Сркезх, олс!", encryptedContent);

//        // Удаление тестовых файлов
//        Files.deleteIfExists(Paths.get(input));
//        Files.deleteIfExists(Paths.get(encryptedFilePath));
    }

    @Test
    public void testDecryptFile() throws IOException {
        // Подготовка входных данных
        String input = "2\ntestFile.txt.encrypted\n3\n";
        provideInput(input);

        // Создание тестового файла
        String testFilePath = "testFile.txt.encrypted";
        Files.write(Paths.get(testFilePath), "Сркезх, олс!".getBytes());

        // Вызов метода main
        Main.main(new String[]{});

        // Проверка вывода
        String expectedOutput = "Выберите режим работы:\n" +
                "1. Шифрование\n" +
                "2. Расшифровка\n" +
                "3. Расшифровка методом brute force (опционально)\n" +
                "4. Расшифровка методом статистического анализа (опционально)\n" +
                "Введите путь к файлу:\n" +
                "Вы ввели корректный путь к файлу: testFile.txt.encrypted\n" +
                "Введите ключ (целое число):\n" +
                "Успех\n";

        assertEquals(expectedOutput, getOutput());

        // Проверка расшифрованного файла
        String decryptedFilePath = "testFile.txt.encrypted.decrypted";
        String decryptedContent = new String(Files.readAllBytes(Paths.get(decryptedFilePath)));
        assertEquals("Привет, мир!", decryptedContent);

        // Удаление тестовых файлов
        Files.deleteIfExists(Paths.get(testFilePath));
        Files.deleteIfExists(Paths.get(decryptedFilePath));
    }

    @Test
    public void testBruteForceDecrypt() throws IOException {
        // Подготовка входных данных
        String input = "3\ntestFile.txt.encrypted\n";
        provideInput(input);

        // Создание тестового файла
        String testFilePath = "testFile.txt.encrypted";
        Files.write(Paths.get(testFilePath), "Сркезх, олс!".getBytes());

        // Вызов метода main
        Main.main(new String[]{});

        // Проверка вывода
        String expectedOutput = "Выберите режим работы:\n" +
                "1. Шифрование\n" +
                "2. Расшифровка\n" +
                "3. Расшифровка методом brute force (опционально)\n" +
                "4. Расшифровка методом статистического анализа (опционально)\n" +
                "Введите путь к файлу:\n" +
                "Вы ввели корректный путь к файлу: testFile.txt.encrypted\n" +
                "Успех\n";

        assertTrue(getOutput().contains(expectedOutput));

        // Проверка расшифрованных файлов
        for (int key = 1; key < 33; key++) {
            String decryptedFilePath = "testFile.txt.encrypted.brute_force_decrypted_" + key;
            if (Files.exists(Paths.get(decryptedFilePath))) {
                String decryptedContent = new String(Files.readAllBytes(Paths.get(decryptedFilePath)));
                if (decryptedContent.equals("Привет, мир!")) {
                    break;
                }
            }
        }

        // Удаление тестовых файлов
        Files.deleteIfExists(Paths.get(testFilePath));
        for (int key = 1; key < 33; key++) {
            String decryptedFilePath = "testFile.txt.encrypted.brute_force_decrypted_" + key;
            Files.deleteIfExists(Paths.get(decryptedFilePath));
        }
    }

    @Test
    public void testInvalidFilePath() {
        // Подготовка входных данных
        String input = "1\ninvalidFile.txt\nvalidFile.txt\n3\n";
        provideInput(input);

        // Вызов метода main
        Main.main(new String[]{});

        // Проверка вывода
        String expectedOutput = "Выберите режим работы:\n" +
                "1. Шифрование\n" +
                "2. Расшифровка\n" +
                "3. Расшифровка методом brute force (опционально)\n" +
                "4. Расшифровка методом статистического анализа (опционально)\n" +
                "Введите путь к файлу:\n" +
                "Неверный путь к файлу или файл пустой. Пожалуйста, попробуйте снова.\n" +
                "Введите путь к файлу:\n" +
                "Вы ввели корректный путь к файлу: validFile.txt\n" +
                "Введите ключ (целое число):\n" +
                "Успех\n";

        assertEquals(expectedOutput, getOutput());
    }

    @Test
    public void testEmptyFile() throws IOException {
        // Подготовка входных данных
        String input = "1\nemptyFile.txt\n";
        provideInput(input);

        // Создание пустого тестового файла
        String testFilePath = "emptyFile.txt";
        Files.write(Paths.get(testFilePath), new byte[0]);

        // Вызов метода main
        Main.main(new String[]{});

        // Проверка вывода
        String expectedOutput = "Выберите режим работы:\n" +
                "1. Шифрование\n" +
                "2. Расшифровка\n" +
                "3. Расшифровка методом brute force (опционально)\n" +
                "4. Расшифровка методом статистического анализа (опционально)\n" +
                "Введите путь к файлу:\n" +
                "Файл пустой. Заполните файл и перезапустите программу.\n";

        assertEquals(expectedOutput, getOutput());

        // Удаление тестового файла
        Files.deleteIfExists(Paths.get(testFilePath));
    }
}