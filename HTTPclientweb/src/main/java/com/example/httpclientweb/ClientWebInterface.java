package com.example.httpclientweb;


 //Interface between client and server

public interface ClientWebInterface {

    void updateServerGame(String Gameid, String gameSituation);
    String hostServerGame(String ServerName);
    String getGameSituation(String serverId);

    String listOfServerGames();
    String joinToServerGame(String serverToJoinGame);
    void leaveServerGame(String serverId, int i);
}


