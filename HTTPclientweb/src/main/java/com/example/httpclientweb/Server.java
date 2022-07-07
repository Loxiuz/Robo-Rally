package com.example.httpclientweb;

import java.util.Arrays;
import java.util.Objects;

public class Server {
    private transient String gameState;
    private int numberOfPlayers;
    private int PlayersOnBoard;
    private transient boolean[] playerSpotFilled;
    private final String id;
    private  String title;

    public Server(String title, int id) {
        this.title=String.valueOf(title);
        this.id = String.valueOf(id);
        this.numberOfPlayers = 1;
    }

    public void addPlayer() {
        numberOfPlayers++;
    }

    public void removePlayer() {
        numberOfPlayers--;
    }

    public boolean isEmpty() {
        return numberOfPlayers == 0;
    }

    public int getAmountOfPlayers() {
        return numberOfPlayers;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public int getPlayersOnBoard() {
        return PlayersOnBoard;
    }

    public void setPlayersOnBoard(int amountOfPlayers) {
        this. PlayersOnBoard = amountOfPlayers;
        this.playerSpotFilled = new boolean[amountOfPlayers];
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
        Server server = (Server) o;
        return numberOfPlayers == server.numberOfPlayers && PlayersOnBoard == server.PlayersOnBoard &&
                id.equals(server.id) && title.equals(server.title) && gameState.equals(server.gameState) &&
                Arrays.equals(playerSpotFilled, server.playerSpotFilled);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, title, gameState, numberOfPlayers, PlayersOnBoard);
        result = 31 * result + Arrays.hashCode(playerSpotFilled);
        return result;
    }

    @Override
    public String toString() {
        return "Server{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", gameState='" + gameState + '\'' +
                ", numberOfPlayers=" + numberOfPlayers +
                ", PlayersOnBoard=" + PlayersOnBoard +
                ", playerSpotFilled=" + Arrays.toString(playerSpotFilled) +
                '}';
    }
}
