package com.example.httpclientweb;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;


@Service
public class GameServerService implements ClientWebInterface {
    ArrayList<GameServer> gameServers = new ArrayList<>();
    private int GameID = 0;
    String jsonFilePath = "roborally/target/classes/SaveGames";


  //Update the id and game state on the server
    @Override
    public void updateServerGame(String id, String gamesituation) {
        GameServer gameserver = findGameID(id);
        gameserver.setGameSituation(gamesituation);
        if (gameserver.getPlayersOnBoard() != 0) //if the max amount of player is set, we are done
            return;
        gameserver.setPlayersOnBoard(StringUtils.countOccurrencesOf(gamesituation, "Player "));
    }

    @Override
    public String joinToServerGame(String serverToJoinGame) {
        GameServer gameserver = findGameID(serverToJoinGame);
        if (gameserver == null)
            return "Server does not exist";
        if (gameserver.getNumberOfPlayers() >= gameserver.getPlayersOnBoard())
            return "The Server is full";
        gameserver.addPlayerOnServer();
        return String.valueOf(gameserver.getRobot());
    }

    @Override
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



    @Override
    public void leaveServerGame(String serverGameId, int robot) {
        GameServer gameserver = findGameID(serverGameId);
        assert gameserver != null;
        gameserver.setPlayerSpotFilled(robot, false);
        gameserver.removePlayerFromServer();
        if (gameserver.ServerisEmpty())
            gameServers.remove(gameserver);
    }

    @Override
    public void saveGame(String json) {
        try {

            FileWriter jsonWriter = new FileWriter(jsonFilePath);
            jsonWriter.write(json);
            jsonWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get game situation from the server and return it in jason file
    @Override
    public String getGameSituation(String serverGameId) {
        return (findGameID(serverGameId)).getGameSituation();
    }


    // use title name of the server and return new gam id
    @Override
    public String hostServerGame(String servername) {
        gameServers.add(new GameServer(servername, GameID));
        String newServerId = String.valueOf(GameID);
        GameID++;
        return newServerId;
    }

    // return list of server
    @Override
    public String listOfServerGames() {
        Gson gson = new Gson();

        ArrayList<GameServer> server = new ArrayList<>();
        gameServers.forEach(e -> {
            if (e.getNumberOfPlayers() != e.getPlayersOnBoard()) {
                server.add(e);
            }
        });
        return gson.toJson(server);
    }


    private GameServer findGameID(String serverGameId) {
        for (GameServer e : gameServers) {
            if (Objects.equals(e.getGameId(), serverGameId)) {
                return e;
            }
        }
        return null;
    }
}