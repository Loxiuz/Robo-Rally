package com.example.httpclientweb;


 //Interface between client and server

public interface ClientWebInterface {

    void updateGame(String Gameid, String gameSituation);
    String hostServerGame(String title);
    String getGameSituation(String serverId);

    String listOfServerGames();
    String joinToGame(String serverToJoin);
    void leaveTheGame(String serverId, int i);
}


