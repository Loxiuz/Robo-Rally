package dk.dtu.compute.se.pisd.httpclient;


 //This interface has some methods which we use them on both client and server .

public interface Client_interface {

    String hostServerGame(String title);
    String listServerGames();
    String joinToAGame(String serverToJoin);

    String getGameSituation();
    void updateServerGame(String gameState);
    void leaveTheGame();
}
