package org.example;

import java.io.*;
import java.util.*;

public class Main {

    private static final char[] ALPHABET_ENG = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int mode = getMode(scanner);
        boolean needKey = mode != 4 && mode != 3; // Ключ не нужен для статистического анализа и brute force

        System.out.println("Введите путь к файлу:");
        String filePath = scanner.nextLine();

        FileValidator fileValidator = new FileValidator(filePath);
        while (!fileValidator.isValidTxtFile()) {
            System.out.println("Введите путь к файлу с расширением .txt:");
            filePath = scanner.nextLine();
            fileValidator.setFilePath(filePath);
        }
        if (fileValidator.isFileEmpty()) {
            System.out.println("Файл пустой. Заполните файл и перезапустите программу.");
            return;
        }

        System.out.println("Вы ввели корректный путь к файлу: " + filePath);

        int key = 0;
        if (needKey) {
            System.out.println("Введите ключ (целое число):");
            key = getKey(scanner);
        }

        CipherProcessor processor = new CipherProcessor(filePath, key);

        switch (mode) {
            case 1:
                processor.encryptFile();
                break;
            case 2:
                processor.decryptFile();
                break;
            case 3:
                processor.bruteForceDecrypt();
                break;
            case 4:
                processor.statisticalAnalysisDecrypt();
                break;
            default:
                System.out.println("Неверный режим работы.");
        }
    }

    private static int getMode(Scanner scanner) {
        int mode = 0;
        boolean validInput = false;

        while (!validInput) {
            System.out.println("Выберите режим работы:");
            System.out.println("1. Шифрование");
            System.out.println("2. Расшифровка");
            System.out.println("3. Расшифровка методом brute force (опционально)");
            System.out.println("4. Расшифровка методом статистического анализа (опционально)");

            if (scanner.hasNextInt()) {
                mode = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера
                if (mode >= 1 && mode <= 4) {
                    validInput = true;
                } else {
                    System.out.println("Неверный режим работы. Пожалуйста, введите число от 1 до 4.");
                }
            } else {
                System.out.println("Неверный ввод. Пожалуйста, введите число.");
                scanner.nextLine(); // Очистка буфера
            }
        }

        return mode;
    }

    private static int getKey(Scanner scanner) {
        int key = 0;
        boolean validInput = false;

        while (!validInput) {
            if (scanner.hasNextInt()) {
                key = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера
                validInput = true;
            } else {
                System.out.println("Неверный ввод. Пожалуйста, введите целое число.");
                scanner.nextLine(); // Очистка буфера
            }
        }

        return key;
    }
}

class FileValidator {
    private String filePath;

    public FileValidator(String filePath) {
        this.filePath = filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isValidTxtFile() {
        if (filePath == null || filePath.isEmpty()) {
            System.out.println("Вы ввели пустую строку");
            return false;
        }

        if (!filePath.toLowerCase().endsWith(".txt")) {
            System.out.println("Введите файл с расширением .txt");
            return false;
        }

        File file = new File(filePath);
        return file.exists() && file.isFile();
    }

    public boolean isFileEmpty() {
        try (FileReader fileReader = new FileReader(filePath)) {
            return fileReader.read() == -1;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }
}

class CipherProcessor {
    private String filePath;
    private int key;
    private static final char[] ALPHABET_ENG = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public CipherProcessor(String filePath, int key) {
        this.filePath = filePath;
        this.key = key;
    }

    public void encryptFile() {
        processFile(filePath, filePath + ".encrypted", this::encrypt);
    }

    public void decryptFile() {
        processFile(filePath, filePath + ".decrypted", this::decrypt);
    }

    public void bruteForceDecrypt() {
        String encryptedText = readFile(filePath);
        for (int key = 1; key < ALPHABET_ENG.length; key++) {
            String decryptedText = decrypt(encryptedText, key);
            if (isTextMeaningful(decryptedText)) {
                System.out.println("Найден правильный ключ: " + key);
                System.out.println("Расшифрованный текст:");
                System.out.println(decryptedText);

                // Запись результата в файл
                String outputFilePath = filePath + ".brute_force_decrypted";
                writeFile(outputFilePath, decryptedText);
                return;
            }
        }
        System.out.println("Не удалось найти правильный ключ.");
    }

    public void statisticalAnalysisDecrypt() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder encryptedText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                encryptedText.append(line).append("\n");
            }

            StatisticalAnalyzer analyzer = new StatisticalAnalyzer(encryptedText.toString(), new String(ALPHABET_ENG));
            int bestKey = analyzer.findBestKey();
            String decryptedText = decrypt(encryptedText.toString(), bestKey);

            System.out.println("Найден правильный ключ: " + bestKey);
            System.out.println("Расшифрованный текст:");
            System.out.println(decryptedText);

            // Запись результата в файл
            String outputFilePath = filePath + ".statistical_analysis_decrypted";

            writeFile(outputFilePath, decryptedText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFile(String inputFilePath, String outputFilePath, CipherFunction cipherFunction) {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(cipherFunction.apply(line));
                writer.newLine();
            }
            System.out.println("Результат записан в файл: " + outputFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String encrypt(String text) {
        return encrypt(text, key);
    }

    private String decrypt(String text) {
        return decrypt(text, key);
    }

    private String decrypt(String text, int key) {
        StringBuilder decryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {
            int index = new String(ALPHABET_ENG).indexOf(Character.toLowerCase(c));
            if (index != -1) {
                int newIndex = (index - key + ALPHABET_ENG.length) % ALPHABET_ENG.length;
                char newChar = ALPHABET_ENG[newIndex];
                decryptedText.append(Character.isUpperCase(c) ? Character.toUpperCase(newChar) : newChar);
            } else {
                decryptedText.append(c);
            }
        }

        return decryptedText.toString();
    }

    private String encrypt(String text, int key) {
        StringBuilder encryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {
            int index = new String(ALPHABET_ENG).indexOf(Character.toLowerCase(c));
            if (index != -1) {
                int newIndex = (index + key) % ALPHABET_ENG.length;
                char newChar = ALPHABET_ENG[newIndex];
                encryptedText.append(Character.isUpperCase(c) ? Character.toUpperCase(newChar) : newChar);
            } else {
                encryptedText.append(c);
            }
        }

        return encryptedText.toString();
    }

    private String readFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void writeFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            System.out.println("Результат записан в файл: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTextMeaningful(String text) {
        String[] commonWords = {"the", "and", "is", "in", "it", "of", "to", "that", "was", "for"};
        for (String word : commonWords) {
            if (text.toLowerCase().contains(word)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    interface CipherFunction {
        String apply(String line);
    }
}

class StatisticalAnalyzer {
    private String text;
    private String alphabet;

    public StatisticalAnalyzer(String text, String alphabet) {
        this.text = text;
        this.alphabet = alphabet;
    }

    public int findBestKey() {
        Map<Character, Integer> frequencyMap = new HashMap<>();

        // Подсчет частоты встречаемости букв
        for (char c : text.toCharArray()) {
            if (alphabet.indexOf(Character.toLowerCase(c)) != -1) {
                frequencyMap.put(Character.toLowerCase(c), frequencyMap.getOrDefault(Character.toLowerCase(c), 0) + 1);
            }
        }

        // Поиск наиболее часто встречающейся буквы
        char mostFrequentChar = ' ';
        int maxFrequency = 0;

        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                maxFrequency = entry.getValue();
                mostFrequentChar = entry.getKey();
            }
        }

        // Предполагаем, что наиболее часто встречающаяся буква в зашифрованном тексте соответствует 'e'
        int key = (alphabet.indexOf(mostFrequentChar) - alphabet.indexOf('e') + alphabet.length()) % alphabet.length();

        return key;
    }
}