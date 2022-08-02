package com.example.httpclientweb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveAndLoad {

    String jsonFilePath = "HTTPclientweb/src/main/resources/Saved/loadTest.json";

    public void saveGame(String json) {

        try {

            FileWriter jsonWriter = new FileWriter(jsonFilePath);
            jsonWriter.write(json);
            jsonWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadGame() {
        Gson gsonFileReader = new Gson();
        JsonObject json = null;
        try {
            Reader jsonFileReader = Files.newBufferedReader(Paths.get(jsonFilePath));
            json = gsonFileReader.fromJson(jsonFileReader, JsonObject.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

}
