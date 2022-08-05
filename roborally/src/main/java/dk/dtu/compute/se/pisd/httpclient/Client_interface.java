package dk.dtu.compute.se.pisd.httpclient;


//This interface has some methods which we use them on both client and server .

import dk.dtu.compute.se.pisd.roborally.model.Board;

public interface Client_interface {

    String hostServerGame(String ServerName);
    String listGamesOnServer();
    String joinToAGame(String serverToJoin);


    String getGameSituation();
    void updateGameSituation(String gameState);
    void leaveServer();
    String loadGame();
    public void saveBoard(Board board);


}
