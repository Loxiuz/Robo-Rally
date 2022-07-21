package com.example.httpclientweb;

import java.util.Arrays;
import java.util.Objects;

public class GameServer {
    private transient String GameSituation;
    private int numberOfPlayers;
    private int PlayersOnBoard;
    private transient boolean[] playerSpotFilled;
    private final String GameId;
    private  String ServerName;

    public GameServer(String ServerName, int Gameid) {
        this.ServerName=String.valueOf(ServerName);
        this.GameId = String.valueOf(Gameid);
        this.numberOfPlayers = 1;
    }

    public void addPlayerOnServer() {
        numberOfPlayers++;
    }

    public void removePlayerFromServer() {
        numberOfPlayers--;
    }

    public boolean ServerisEmpty() {
        return numberOfPlayers == 0;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public String getGameSituation() {
        return GameSituation;
    }

    public void setGameSituation(String gameState) {
        this.GameSituation = gameState;
    }

    public String getGameId() {
        return GameId;
    }
    public String getServerName() {
        return ServerName;
    }

    public int getPlayersOnBoard() {
        return PlayersOnBoard;
    }

    public void setPlayersOnBoard(int numberOfPlayers) {
        this. PlayersOnBoard = numberOfPlayers;
        this.playerSpotFilled = new boolean[numberOfPlayers];
        playerSpotFilled[0] = true;
    }

    public void setPlayerSpotFilled(int i, boolean flag) {
        playerSpotFilled[i] = flag;
    }

    public int getRobot() {
        for (int i = 0; i <  PlayersOnBoard; i++)
            if (!playerSpotFilled[i]) {
                playerSpotFilled[i] = true;
                return i;
            }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameServer server = (GameServer) o;
        return numberOfPlayers == server.numberOfPlayers && PlayersOnBoard == server.PlayersOnBoard &&
                GameId.equals(server.GameId) && ServerName.equals(server.ServerName) && GameSituation.equals(server.GameSituation) &&
                Arrays.equals(playerSpotFilled, server.playerSpotFilled);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(GameId, GameSituation, GameSituation, numberOfPlayers, PlayersOnBoard);
        result = 31 * result + Arrays.hashCode(playerSpotFilled);
        return result;
    }

    @Override
    public String toString() {
        return "Server{" +
                "id='" + GameId + '\'' +
                ", title='" + ServerName + '\'' +
                ", gameState='" + GameSituation + '\'' +
                ", numberOfPlayers=" + numberOfPlayers +
                ", PlayersOnBoard=" + PlayersOnBoard +
                ", playerSpotFilled=" + Arrays.toString(playerSpotFilled) +
                '}';
    }
}
