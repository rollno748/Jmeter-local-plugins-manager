package io.perfwise.local.pluginsmanager.controller;

import org.json.JSONArray;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class Rest {

    private static final Logger LOGGER = Logger.getLogger(Rest.class.getName());
    private static JSONArray jsonArr = null;
    private static InputStream is = null;
    private static File file;

    public static JSONArray get(String url) throws MalformedURLException, IOException {
        try {
            is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            jsonArr = new JSONArray(jsonText);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        return jsonArr;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


    public static void Downloader(String filePath, URL url) throws MalformedURLException, IOException {

        int respCode = getResponseCode(url);
        file = new File (filePath + url.toString().substring(url.toString().lastIndexOf("/")));

        if (respCode == 200) {

            try {
                InputStream input = url.openStream();
                if (file.exists()) {
                    if (file.isDirectory())
                        throw new IOException("File '" + file + "' is a directory");

                    if (!file.canWrite())
                        throw new IOException("File '" + file + "' cannot be written");
                } else {
                    File parent = file.getParentFile();
                    if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
                        throw new IOException("File '" + file + "' could not be created");
                    }
                }

                FileOutputStream output = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n);
                }

                input.close();
                output.close();

                LOGGER.info(file + " downloaded successfully!");
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }
        }
    }


    public static int getResponseCode(URL url) throws MalformedURLException, IOException {

        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setRequestMethod("GET");
        huc.connect();
        return huc.getResponseCode();
    }

}
