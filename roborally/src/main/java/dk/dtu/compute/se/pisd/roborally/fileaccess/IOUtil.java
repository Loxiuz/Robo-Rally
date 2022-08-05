package dk.dtu.compute.se.pisd.roborally.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.exceptions.BoardDoesNotExistException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//
// Do a specific path in resources folder , read and write serialized boards from the json file
public class IOUtil {
    private static final String SaveGames = "SaveGames";
    private static final String GameBoardsJson = "GameBoardsJson";
    private static final String JsonFile = "json";


    // this methode uses for saving boards in Json
    public static String writeGameJson(String saveJsonName, String json) {
        // Saving the board template using JSON
        //class loader has responsible for loading classes.
        ClassLoader classLoader = IOUtil.class.getClassLoader();

        String jsonName = Objects.requireNonNull(classLoader.getResource(SaveGames)).getPath() + "/"
                + saveJsonName + "." + JsonFile;

        GsonBuilder jsonBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        Gson gson = jsonBuilder.create();

        FileWriter fileWriter = null;
        JsonWriter jsonwriter = null;
        jsonName = jsonName.replaceAll("%20", " ");
        try {
            fileWriter = new FileWriter(jsonName);
            jsonwriter = gson.newJsonWriter(fileWriter);

            jsonwriter.jsonValue(json);
            jsonwriter.flush();

            jsonwriter.close();
        } catch (IOException e1) {
            if (jsonwriter != null) {
                try {
                    jsonwriter.close();
                    fileWriter = null;
                } catch (IOException ignored) {
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ignored) {
                }
            }
        }
        return jsonName;
    }


    //Reads game form json in resources folder
    public static String readGameJson(String resourcePath) throws BoardDoesNotExistException {
        try {
            ClassLoader classLoader = IOUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            if (inputStream == null) throw new BoardDoesNotExistException(resourcePath);

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8); // Eight-bit UCS Transformation Format.
        } catch (IOException e) {
            throw new BoardDoesNotExistException(resourcePath);
        }
    }


    // this methode gets the name of saved boards form resources folder
    public static List<String> getSavedBoardsName() {
        File[] Files_list = getFilesInResources(SaveGames);
        List<String> fileNames = new ArrayList<>();

        for (File file : Files_list) {
            fileNames.add(file.getName().replace(".json", ""));
        }

        return fileNames;
    }


    // this method gets name of default boards form resources folder
    public static List<String> getBoardGameName() {
        File[] Files_list = getFilesInResources(GameBoardsJson);
        List<String> jsonNames = new ArrayList<>();

        for (File file : Files_list) {
            jsonNames.add(file.getName().replace(".json", ""));
        }

        return jsonNames;
    }


    // get files in the resource folder
    private static File[] getFilesInResources(String resourcesName) {
        ClassLoader classLoader = IOUtil.class.getClassLoader();
        String Path = Objects.requireNonNull(classLoader.getResource(resourcesName)).getPath();


        Path = Path.replace("json", " ");

        File folder = new File(Path);

        return folder.listFiles();
    }
}
