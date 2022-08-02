package dk.dtu.compute.se.pisd.roborally.exceptions;


public class BoardDoesNotExistException extends Exception {
    private final String boardPath;

    /**
     * Exception used for when a board game does not exist.
     */
    public BoardDoesNotExistException(String boardPath){
        this.boardPath = boardPath;
    }

    public String getBoardPath(){
        return this.boardPath;
    }
}
