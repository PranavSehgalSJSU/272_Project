package com.model;

///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : Auth0Model.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Simple static Auth0 utility for token generation.
//  NOTE: Uses PropertyReader to pull values from application.properties
///////////////////////////////////////////////////////////////////////////////////////////////////////

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.persistance.Database.PropertyReader;

public class Auth0Model {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    //  METHOD : getToken
    //  DESCRIPTION: Requests an Auth0 token using client credentials
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getToken() {
        try {
            String domain = PropertyReader.getProperty("auth0.domain");
            String clientId = PropertyReader.getProperty("auth0.clientId");
            String clientSecret = PropertyReader.getProperty("auth0.clientSecret");
            String audience = PropertyReader.getProperty("auth0.audience");

            URL url = new URL("https://" + domain + "/oauth/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{"
                    + "\"client_id\":\"" + clientId + "\","
                    + "\"client_secret\":\"" + clientSecret + "\","
                    + "\"audience\":\"" + audience + "\","
                    + "\"grant_type\":\"client_credentials\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes("utf-8"));
            }

            int code = conn.getResponseCode();
            BufferedReader br;
            if (code == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            conn.disconnect();
            String token = response.toString().replaceAll(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
            return token;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
