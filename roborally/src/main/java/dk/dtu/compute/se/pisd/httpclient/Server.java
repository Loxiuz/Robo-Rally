package dk.dtu.compute.se.pisd.httpclient;

/**
 * This class uses for Json to determine how the string from the server is declared.
 */
public class Server {

    private String GameId;
    private String ServerName;
    private transient String GameSituation;
    private int numberOfPlayers;
    private int PlayersOnBoard;
    private transient boolean[] playerSpotFilled;

    public String getGameId() {
        return GameId;
    }

    public String getServerName() {
        return ServerName;
    }

    public int getnumberOfPlayers() {
        return numberOfPlayers;
    }

    public int getPlayersOnBoard() {
        return PlayersOnBoard;
    }
}
