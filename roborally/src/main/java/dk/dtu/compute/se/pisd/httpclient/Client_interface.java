package dk.dtu.compute.se.pisd.httpclient;


 //

import dk.dtu.compute.se.pisd.roborally.model.Board;

/**
 * This interface has some methods which is used on both client and server.
 */
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
