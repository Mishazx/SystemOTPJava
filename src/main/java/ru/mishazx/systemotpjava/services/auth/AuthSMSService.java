package ru.mishazx.systemotpjava.services.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AuthSMSService {
    private static final String API_ID = "5909415B-8F04-506F-BB1B-3E46DD5A438F";
    private static final String BASE_URL = "https://sms.ru/sms/send";
    private final boolean isTestMode; // Флаг для переключения между реальным и тестовым режимом

    public AuthSMSService(boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    public Map<String, Object> sendSms(String numbers, String message) {
        if (isTestMode) {
            return sendFakeSms(numbers, message);
        } else {
            return sendRealSms(numbers, message);
        }
    }

    private Map<String, Object> sendRealSms(String numbers, String message) {
        String urlString = String.format("%s?api_id=%s&to=%s&msg=%s&json=1",
                BASE_URL, API_ID, numbers, message);
        Map<String, Object> response = new HashMap<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder responseBuilder = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    responseBuilder.append(inputLine);
                }
                in.close();
                // Здесь вы можете разобрать JSON-ответ и вернуть его в виде Map
                // Для простоты, просто возвращаем строку
                response.put("response", responseBuilder.toString());
            } else {
                response.put("error", "Error: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "IOException: " + e.getMessage());
        }

        return response;
    }

    private Map<String, Object> sendFakeSms(String numbers, String message) {
        // Имитируем ответ сервера
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("status_code", 100);

        Map<String, Object> smsStatus = new HashMap<>();
        String[] numberList = numbers.split(",");

        for (String number : numberList) {
            // Имитируем успешную отправку для первого номера и ошибку для второго
            if (number.equals("79153562460")) {
                Map<String, Object> smsInfo = new HashMap<>();
                smsInfo.put("status", "OK");
                smsInfo.put("status_code", 100);
                smsInfo.put("sms_id", "000000-10000000");
                smsStatus.put(number, smsInfo);
            } else {
                Map<String, Object> smsInfo = new HashMap<>();
                smsInfo.put("status", "ERROR");
                smsInfo.put("status_code", 207);
                smsInfo.put("status_text", "На этот номер (или один из номеров) нельзя отправлять сообщения, либо указано более 100 номеров в списке получателей");
                smsStatus.put(number, smsInfo);
            }
        }

        response.put("sms", smsStatus);
        response.put("balance", 4122.56);

        return response;
    }
}
