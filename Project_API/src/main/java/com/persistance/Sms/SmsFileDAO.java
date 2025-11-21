package com.persistance.Sms;
///////////////////////////////////////////////////////////////////////////////////////////////////////
//  FILE : SmsFileDAO.java
//  AUTHOR : Pranav Sehgal <PranavSehgalSJSU>
//  DESCRIPTION: Sends SMS via Textbelt API using Apache HttpClient.
///////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.persistance.Database.PropertyReader;

public class SmsFileDAO implements SmsDAO {

    private static final String API_URL = "http://textbelt.com/text";
    private static final String API_KEY = PropertyReader.getProperty("textbelt.apikey");

    @Override
    public void sendSmsTo(String phoneNumber, String content) {
        try {
            System.out.println("Using Textbelt API key: " + API_KEY);

            List<NameValuePair> data = Arrays.asList(
                new BasicNameValuePair("phone", phoneNumber),
                new BasicNameValuePair("message", content),
                new BasicNameValuePair("key", API_KEY)
            );

            HttpClient httpClient = HttpClients.createMinimal();
            HttpPost httpPost = new HttpPost(API_URL);
            httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            JSONObject response = new JSONObject(responseString);
            System.out.println("SMS API Response: " + response.toString(4));

        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
        }
    }

    @Override
    public void sendVerificationSms(String username, String phoneNumber) {
        String link = PropertyReader.getProperty("api.link");
        String verifyLink = link+"/auth/verify?username=" + username + "&type=phone";
        String message = "Hey " + username + "!\n\n"
                + "Please verify your phone number:\n"
                + verifyLink + "\n\n";
        sendSmsTo(phoneNumber, message);
    }
}
