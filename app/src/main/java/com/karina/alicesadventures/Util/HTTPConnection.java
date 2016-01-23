package com.karina.alicesadventures.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by karina on 2016-01-06.
 */
public class HTTPConnection {
    // private final String USER_AGENT = "Mozilla/5.0";
    public static final String SERVER_BASE_URL ="https://karinanishimura.com.br/echo_practice/";
    public static void main(String[] args) throws Exception {

        HTTPConnection http = new HTTPConnection();

        // System.out.println("Testing 1 - Send Http GET request");
        // http.sendGet();

        System.out.println("\nTesting 2 - Send Http POST request");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("data[User][password]", "t");
        hashMap.put("data[User][username]", "t");
        http.sendPost(HTTPConnection.SERVER_BASE_URL+"users/login_api.xml", hashMap);

    }

    public HTTPConnection() {
    }

    // HTTP GET request
    public String sendGet(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        //   con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        con.setRequestProperty("Accept", "text/xml; charset=utf-8");

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();

    }

    // HTTP POST request
    public String sendPost(String url, HashMap<String, String> urlParameters) throws Exception {
        if (url != null) {
            URL obj = new URL(url);
//        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "text/xml; charset=utf-8");

            //   urlParameters= "data[User][username]=thiago&data[User][password]=thiago";

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            String sParams = getQuery(urlParameters);
            wr.writeBytes(sParams);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("Response Code : " + response.toString());

            //print result
            return response.toString();
        }
        return null;
    }

    private String getQuery(HashMap<String, String> params) {
        String result = "";
        boolean first = true;

        for (String pair : params.keySet()) {
            if (first)
                first = false;
            else
                result += "&";

            result += pair;
            result += "=";
            result += params.get(pair);
        }

        return result;
    }
}
