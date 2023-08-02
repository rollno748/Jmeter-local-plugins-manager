package io.perfwise.local.pluginsmanager.scheduler.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final int MAX_RETRIES = 10;
    private static final long RETRY_INTERVAL_MS = 60000;

//    public static JSONArray get(String url) throws IOException {
//        HttpGet httpGet = new HttpGet(url);
//        HttpResponse response = HTTP_CLIENT.execute(httpGet);
//        HttpEntity entity = response.getEntity();
//        JSONArray jsonArray = new JSONArray(EntityUtils.toString(entity));
//        EntityUtils.consume(entity);
//        return jsonArray;
//    }

    public static JSONArray get(String url) throws IOException {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = HTTP_CLIENT.execute(httpGet);
                HttpEntity entity = response.getEntity();
                JSONArray jsonArray = new JSONArray(EntityUtils.toString(entity));
                EntityUtils.consume(entity);
                return jsonArray;
            } catch (IOException e) {
                System.err.println("Attempt " + (retries + 1) + " failed. Retrying in " + (RETRY_INTERVAL_MS / 1000) + " seconds...");
                retries++;
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException interruptedException) {
                    // Ignore interruption during sleep
                }
            }
        }
        throw new IOException("Failed to execute HTTP GET request after " + MAX_RETRIES + " retries.");
    }

    public static void fileDownloader(String filePath, URL url) throws IOException {
        int respCode = getResponseCode(url);
        if (respCode == 200) {
            File file = new File(filePath + url.toString().substring(url.toString().lastIndexOf("/")));

            try (InputStream input = url.openStream();
                 FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = input.read(buffer)) != -1) {
                    output.write(buffer, 0, n);
                }
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }

            LOGGER.info("{} downloaded successfully!", file);
        }
    }

    public static int getResponseCode(URL url) throws IOException {
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        return huc.getResponseCode();
    }
}
