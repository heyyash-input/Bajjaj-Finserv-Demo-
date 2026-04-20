package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
public class BajajTaskRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 1. STEP 1: Generate the Webhook
        String registrationUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> regRequest = new HashMap<>();
        regRequest.put("name", "Yash Patil");
        regRequest.put("regNo", "adt23socb1352"); // Your EVEN reg number [cite: 19]
        regRequest.put("email", "yashpatil8930@gmail.com");

        System.out.println("Attempting Registration...");

        try {
            // First POST to get instructions [cite: 8]
            ResponseEntity<Map> regResponse = restTemplate.postForEntity(registrationUrl, regRequest, Map.class);

            if (regResponse.getStatusCode() == HttpStatus.OK && regResponse.getBody() != null) {
                Map<String, Object> body = regResponse.getBody();

                // Extracting unique URL and Access Token [cite: 17, 18]
                String webhookUrl = (String) body.get("webhookUrl");
                String accessToken = (String) body.get("accessToken");

                // Check if URI is null to avoid "not absolute" error
                if (webhookUrl == null || webhookUrl.isEmpty()) {
                    System.err.println("CRITICAL: Bajaj server returned a null URL. Please check your regNo.");
                    return;
                }

                System.out.println("Webhook URL Received: " + webhookUrl);

                // 2. STEP 2: The SQL Solution (Question 2 - Even) [cite: 19, 79]
                // Calculates employees in the same department who are younger (higher DOB) [cite: 81, 82]
                String solvedQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                        "(SELECT COUNT(*) FROM EMPLOYEE e2 WHERE e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB) " +
                        "AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 " +
                        "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                        "ORDER BY e1.EMP_ID DESC;"; // Descending order [cite: 90]

                // 3. STEP 3: Submit Solution to Webhook [cite: 24, 25]
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", accessToken); // Authorization header with JWT [cite: 27, 37]
                headers.setContentType(MediaType.APPLICATION_JSON); // [cite: 28]

                Map<String, String> submitBody = new HashMap<>();
                submitBody.put("finalQuery", solvedQuery); // [cite: 31]

                HttpEntity<Map<String, String>> entity = new HttpEntity<>(submitBody, headers);

                System.out.println("Submitting final query to webhook...");
                ResponseEntity<String> finalResult = restTemplate.postForEntity(webhookUrl, entity, String.class);
                System.out.println("Submission Success! Response: " + finalResult.getBody());
            }
        } catch (Exception e) {
            System.err.println("Process Failed: " + e.getMessage());
            // Even if it fails here, push your code to GitHub for grading!
        }
    }
}