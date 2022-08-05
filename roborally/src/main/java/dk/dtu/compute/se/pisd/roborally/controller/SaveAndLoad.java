package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.StartGear;
import dk.dtu.compute.se.pisd.roborally.exceptions.BoardDoesNotExistException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.IOUtil;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Saving and Loading games of Roborally
 */
public class SaveAndLoad {

    final static private List<String> PLAYERCOLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
    private static final String JSONFile = "json";
    private static final String SAVEDBOARDS = "SaveGames";
    private static final String BOARDS = "GameBoardsJson";
    private static boolean NewBoard = false;


    // Get the players board and it's data and seves the game in a file

    /**
     * Saves a game to Json.
     * @param board
     * @param name
     */
    public static void SaveBoardGame(Board board, String name) {
        String resource = SAVEDBOARDS + "/" + name + "." + JSONFile;

        // Setting up the board template
        String json = dk.dtu.compute.se.pisd.roborally.fileaccess.SaveAndLoad.serialize(board);

        IOUtil.writeGameJson(name, json);

    }


    /**
     * Loads a game from Json.
     * @param name Filename of the board/game
     * @return Returns the loaded game
     * @throws BoardDoesNotExistException In case the board name does not exist.
     */
    public static Board loadBoardGame(String name) throws BoardDoesNotExistException {
        String resourcePath = SAVEDBOARDS + "/" + name + "." + JSONFile;
        String json = IOUtil.readGameJson(resourcePath);

        return dk.dtu.compute.se.pisd.roborally.fileaccess.SaveAndLoad.deserialize(json, true);

    }

    /**
     * Creates a new board.
     * @param numPlayers Number of players in the game.
     * @param boardName Name of the board.
     * @return Returns the created board
     * @throws BoardDoesNotExistException
     */
    public static Board newBoard(int numPlayers, String boardName) throws BoardDoesNotExistException {
        NewBoard = true;


        String resourcePath = BOARDS + "/" + boardName + "." + JSONFile;
        String json = IOUtil.readGameJson(resourcePath);

        Board board = dk.dtu.compute.se.pisd.roborally.fileaccess.SaveAndLoad.deserialize(json, false);

        // Create the players and place them
        for (int i = 0; i < numPlayers; i++) {
            Player newPlayer = new Player(board, PLAYERCOLORS.get(i), "Player " + (i + 1));
            board.addPlayer(newPlayer);
        }

        List<Space> startGears = getSpacesFieldAction(board, new StartGear());
        PlayersPlace(board.getPlayers(), startGears);

        return board;
    }



    public static boolean getNewBoardCreated() {
        return NewBoard;
    }

    /**
     * Places the players at possible positions
     * @param players List of players to be places
     * @param possibleSpaces List of the possible spaces.
     */
    private static void PlayersPlace(List<Player> players, List<Space> possibleSpaces) {

        for (Player currentPlayer : players) {
            Space currentSpace = possibleSpaces.get(0);

            currentPlayer.setSpace(currentSpace);
            possibleSpaces.remove(currentSpace);

            currentPlayer.setHeading(Heading.EAST);
        }
    }

    /**
     * A get method to get all spaces with a certain field action.
     * @param board Relevant board.
     * @param action A field action
     * @return Returns list with to relevant spaces.
     */
    private static List<Space> getSpacesFieldAction(Board board, FieldAction action) {
        List<Space> spaces = new ArrayList<>();

        for (int y = 0; y < board.height; y++) {
            for (int x = 0; x < board.width; x++) {
                Space curSpace = board.getSpace(x, y);
                List<FieldAction> curSpaceActions = curSpace.getActions();

                if (curSpaceActions.size() == 0)
                    continue;

                String curFieldActionName = curSpaceActions.get(0).getClass().getSimpleName();
                if (curFieldActionName.equals(action.getClass().getSimpleName())) {
                    spaces.add(curSpace);
                }
            }
        }
        return spaces;
    }

}
