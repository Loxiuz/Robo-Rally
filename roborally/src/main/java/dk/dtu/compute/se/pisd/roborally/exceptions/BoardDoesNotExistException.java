package dk.dtu.compute.se.pisd.roborally.exceptions;


public class BoardDoesNotExistException extends Exception {
    private final String gameboards;

    // exception uses for when a board game does not exist

    public BoardDoesNotExistException(String gameboards){
        this.gameboards = gameboards;
    }

    public String getBoardPath(){
        return this.gameboards;
    }
}
