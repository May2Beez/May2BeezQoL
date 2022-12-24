package com.May2Beez.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new Gson();

    private static final JsonParser parser = new JsonParser();
    public static List<String> getListFromUrl(String url, String name) {
        List<String> ret = new ArrayList<>();
        try {
            JsonObject json = (JsonObject)parser.parse(getContent(url));
            json.getAsJsonArray(name).forEach(je -> ret.add(je.getAsString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getContent(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);
        in.close();
        return response.toString();
    }
}
