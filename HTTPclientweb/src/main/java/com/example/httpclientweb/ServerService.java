package com.example.httpclientweb;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Objects;


@Service
public class ServerService implements ClientWebInterface {
    ArrayList<Server> servers = new ArrayList<>();
    private int GameID = 0;

  //Update the id and game state on the server
    @Override
    public void updateGame(String id, String gameState) {
        Server server = findServerID(id);
        server.setGameState(gameState);
        if (server.getPlayersOnBoard() != 0) //if the max amount of player is set, we are done
            return;
        server.setPlayersOnBoard(StringUtils.countOccurrencesOf(gameState, "Player "));
    }

    @Override
    public String joinToGame(String serverToJoin) {
        Server s = findServerID(serverToJoin);
        if (s == null)
            return "Server does not exist";
        if (s.getAmountOfPlayers() >= s.getPlayersOnBoard())
            return "The Server is full";
        s.addPlayer();
        return String.valueOf(s.getRobot());
    }


    @Override
    public void leaveTheGame(String serverId, int robot) {
        Server server = findServerID(serverId);
        assert server != null;
        server.setPlayerSpotFilled(robot, false);
        server.removePlayer();
        if (server.isEmpty())
            servers.remove(server);
    }

    // get game state from the server and return it in jason file
    @Override
    public String getGameState(String serverId) {
        return (findServerID(serverId)).getGameState();
    }


    // use title name of the server and return new gam id
    @Override
    public String hostGame(String title) {
        servers.add(new Server(title, GameID));
        String newServerId = String.valueOf(GameID);
        GameID++;
        return newServerId;
    }

    // return list of server
    @Override
    public String listOfGames() {
        Gson gson = new Gson();

        ArrayList<Server> server = new ArrayList<>();
        servers.forEach(e -> {
            if (e.getAmountOfPlayers() != e.getPlayersOnBoard()) {
                server.add(e);
            }
        });
        return gson.toJson(server);
    }


    private Server findServerID(String serverId) {
        for (Server e : servers) {
            if (Objects.equals(e.getId(), serverId)) {
                return e;
            }
        }
        return null;
    }
}