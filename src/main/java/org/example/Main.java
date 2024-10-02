package org.example;

import java.io.*;
import java.util.*;

public class Main {

    private static final String ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int mode = getMode(scanner);

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

        System.out.println("Введите ключ (целое число):");
        int key = getKey(scanner);

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
    private int finalkey = key;
    private static final String ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";

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
        for (int key = 1; key < ALPHABET.length(); key++) {
            processFile(filePath, filePath + ".brute_force_decrypted_" + key, line -> decrypt(line, finalkey));
        }
    }

    public void statisticalAnalysisDecrypt() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder encryptedText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                encryptedText.append(line).append("\n");
            }

            StatisticalAnalyzer analyzer = new StatisticalAnalyzer(encryptedText.toString(), ALPHABET);
            int bestKey = analyzer.findBestKey();
            processFile(filePath, filePath + ".statistical_analysis_decrypted", encryptedLine -> decrypt(encryptedLine, bestKey));
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
            System.out.println("Успех");
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
        return encrypt(text, ALPHABET.length() - key);
    }

    private String encrypt(String text, int key) {
        StringBuilder encryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {
            int index = ALPHABET.indexOf(c);
            if (index != -1) {
                int newIndex = (index + key) % ALPHABET.length();
                encryptedText.append(ALPHABET.charAt(newIndex));
            } else {
                encryptedText.append(c);
            }
        }

        return encryptedText.toString();
    }

    @FunctionalInterface
    interface CipherFunction {
        String apply(String line);
    }
}

class StatisticalAnalyzer {
    private String encryptedText;
    private String alphabet;

    public StatisticalAnalyzer(String encryptedText, String alphabet) {
        this.encryptedText = encryptedText;
        this.alphabet = alphabet;
    }

    public int findBestKey() {
        Map<Character, Double> russianFrequency = getRussianFrequency();
        double bestScore = Double.MAX_VALUE;
        int bestKey = 0;

        for (int key = 0; key < alphabet.length(); key++) {
            String decryptedText = decrypt(encryptedText, key);
            Map<Character, Integer> frequency = getFrequency(decryptedText);
            double score = calculateScore(frequency, russianFrequency);

            if (score < bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }

        return bestKey;
    }

    private String decrypt(String text, int key) {
        StringBuilder decryptedText = new StringBuilder();

        for (char c : text.toCharArray()) {
            int index = alphabet.indexOf(c);
            if (index != -1) {
                int newIndex = (index - key + alphabet.length()) % alphabet.length();
                decryptedText.append(alphabet.charAt(newIndex));
            } else {
                decryptedText.append(c);
            }
        }

        return decryptedText.toString();
    }

    private Map<Character, Double> getRussianFrequency() {
        Map<Character, Double> frequency = new HashMap<>();
        frequency.put('о', 0.1097);
        frequency.put('е', 0.0845);
        frequency.put('а', 0.0801);
        frequency.put('и', 0.0735);
        frequency.put('н', 0.0670);
        frequency.put('т', 0.0626);
        frequency.put('с', 0.0547);
        frequency.put('р', 0.0473);
        frequency.put('в', 0.0454);
        frequency.put('л', 0.0440);
        frequency.put('к', 0.0349);
        frequency.put('м', 0.0321);
        frequency.put('д', 0.0298);
        frequency.put('п', 0.0281);
        frequency.put('у', 0.0262);
        frequency.put('я', 0.0201);
        frequency.put('ы', 0.0190);
        frequency.put('ь', 0.0174);
        frequency.put('г', 0.0170);
        frequency.put('з', 0.0165);
        frequency.put('б', 0.0159);
        frequency.put('ч', 0.0144);
        frequency.put('й', 0.0121);
        frequency.put('х', 0.0097);
        frequency.put('ж', 0.0094);
        frequency.put('ш', 0.0073);
        frequency.put('ю', 0.0064);
        frequency.put('ц', 0.0048);
        frequency.put('щ', 0.0036);
        frequency.put('э', 0.0032);
        frequency.put('ф', 0.0026);
        frequency.put('ъ', 0.0004);
        frequency.put('ё', 0.0004);

        return frequency;
    }

    private Map<Character, Integer> getFrequency(String text) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char c : text.toCharArray()) {
            if (alphabet.indexOf(c) != -1) {
                frequency.put(c, frequency.getOrDefault(c, 0) + 1);
            }
        }
        return frequency;
    }

    private double calculateScore(Map<Character, Integer> frequency, Map<Character, Double> referenceFrequency) {
        double score = 0;
        int totalChars = frequency.values().stream().mapToInt(Integer::intValue).sum();

        for (char c : referenceFrequency.keySet()) {
            double expected = referenceFrequency.get(c);
            double actual = frequency.getOrDefault(c, 0) / (double) totalChars;
            score += Math.pow(expected - actual, 2);
        }

        return score;
    }
}