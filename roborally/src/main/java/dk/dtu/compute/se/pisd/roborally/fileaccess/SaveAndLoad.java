package dk.dtu.compute.se.pisd.roborally.fileaccess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.StartGear;
import dk.dtu.compute.se.pisd.roborally.exceptions.BoardDoesNotExistException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.*;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


 // Saving and Loading games of Roborally

public class SaveAndLoad {

    final static private List<String> PLAYERCOLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
    private static final String JSONFile = "json";
    private static final String SAVEDBOARDS = "SaveGames";
    private static final String BOARDS = "GameBoardsJson";
    private static boolean NewBoard = false;
    private static final String SaveGames= "roborally/src/main/resources/SaveGames";

    public static String serialize(Board board) {
        // board and space template
        BoardTemplate boardtemplate = new BoardTemplate();
        boardtemplate.width = board.width;
        boardtemplate.height = board.height;
        boardtemplate.phase = board.phase.toString();
        boardtemplate.step = board.step;
        boardtemplate.stepMode = board.stepMode;
        boardtemplate.gameOver = board.gameOver;




        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.height; j++) {
                Space space = board.getSpace(i, j);
                if (!space.getWalls().isEmpty() || !space.getActions().isEmpty()) {
                    SpaceTemplate spaceTemplate = new SpaceTemplate();
                    spaceTemplate.x = space.x;
                    spaceTemplate.y = space.y;
                    spaceTemplate.actions.addAll(space.getActions());
                    spaceTemplate.walls.addAll(space.getWalls());
                    boardtemplate.spaces.add(spaceTemplate);
                }
            }
        }

        // Setting up players
        List<Player> players = board.getPlayers();
        List<PlayerTemplate> playersTemplate = new ArrayList<>();

        for (Player player : players) {
            PlayerTemplate playerTemplate = new PlayerTemplate();
            CommandCardFieldTemplate[] programTemplate = new CommandCardFieldTemplate[player.program.length];
            CommandCardFieldTemplate[] cardsTemplate = new CommandCardFieldTemplate[player.cards.length];

            playerTemplate.name = player.name;
            playerTemplate.color = player.color;
            playerTemplate.checkPoints = player.checkPoints;
            playerTemplate.priority = player.priority;
            playerTemplate.spaceX = player.space.x;
            playerTemplate.spaceY = player.space.y;
            playerTemplate.heading = player.heading.toString();

            // Saving players cards
            for (int j = 0; j < player.cards.length; j++) {
                CommandCardField card = player.cards[j];
                CommandCardFieldTemplate commandCardFieldTemplate = new CommandCardFieldTemplate();
                CommandCardTemplate commandCardTemplate = new CommandCardTemplate();
                CommandTemplate commandTemplate = new CommandTemplate();

                // The command of the card
                if (card.card == null) {
                    commandTemplate.type = "";
                } else {
                    commandTemplate.type = card.card.command.name();
                    List<String> options = new ArrayList<>();
                    for (Command option : card.card.command.options) {
                        options.add(String.valueOf(option));
                    }
                }
                // The command card
                commandCardTemplate.command = commandTemplate;

                // Command Card Field
                commandCardFieldTemplate.card = commandCardTemplate;
                commandCardFieldTemplate.visible = card.visible;

                cardsTemplate[j] = commandCardFieldTemplate;
            }

            // Saving players registers
            for (int j = 0; j < player.program.length; j++) {
                CommandCardField card = player.program[j];
                CommandCardFieldTemplate commandCardFieldTemplate = new CommandCardFieldTemplate();
                CommandCardTemplate commandCardTemplate = new CommandCardTemplate();
                CommandTemplate commandTemplate = new CommandTemplate();

                // The command of the card
                if (card.card == null) {
                    commandTemplate.type = "";
                } else {
                    commandTemplate.type = card.card.command.name();
                    List<String> options = new ArrayList<>();
                    for (Command option : card.card.command.options) {
                        options.add(String.valueOf(option));
                    }
                }
                // The command card
                commandCardTemplate.command = commandTemplate;

                // Command Card Field
                commandCardFieldTemplate.card = commandCardTemplate;
                commandCardFieldTemplate.visible = card.visible;

                programTemplate[j] = commandCardFieldTemplate;
            }

            playerTemplate.program = programTemplate;
            playerTemplate.cards = cardsTemplate;

            playersTemplate.add(playerTemplate);
        }
        boardtemplate.players = playersTemplate;
        if (board.getCurrentPlayer() == null)
            boardtemplate.currentPlayer = 0;
        else
            boardtemplate.currentPlayer = board.getPlayerNumber(board.getCurrentPlayer());


        // Saving the board template using GSON
        GsonBuilder jsonBuilder = new GsonBuilder().
                registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>()).
                setPrettyPrinting();
        Gson gson = jsonBuilder.create();

        return gson.toJson(boardtemplate, boardtemplate.getClass());
    }

    // Get the players board and it's data and seves the game in a file
    public static void SaveBoardGame(Board board, String name) {
       // String resourcePath = SAVEDBOARDS + "/" + name + "." + JSONFile;

        // Setting up the board template
        String json = serialize(board);
        IOUtil.writeGameJson(name, json);

    }


    public static Board deserialize(String jsonString, boolean savedGame) {
        GsonBuilder JsonBuilder = new GsonBuilder().registerTypeAdapter(FieldAction.class, new Adapter<FieldAction>());
        Gson gson = JsonBuilder.create();

        JsonReader reader = gson.newJsonReader(new StringReader(jsonString));
        Board result;

        BoardTemplate boardTemplate = gson.fromJson(reader, BoardTemplate.class);

        // Actual Loading of the board
        result = new Board(boardTemplate.width, boardTemplate.height);

        if (savedGame) {
            result.phase = Phase.valueOf(boardTemplate.phase);
            result.step = boardTemplate.step;
            result.stepMode = boardTemplate.stepMode;
            result.gameOver = boardTemplate.gameOver;

        }

        for (SpaceTemplate spaceTemplate : boardTemplate.spaces) {
            Space space = result.getSpace(spaceTemplate.x, spaceTemplate.y);
            if (space != null) {
                space.getActions().addAll(spaceTemplate.actions);
                space.getWalls().addAll(spaceTemplate.walls);
                space.setPlayer(null);
            }
        }
        result.setCheckpoints_Number();

        // Loading Players
        for (int i = 0; i < boardTemplate.players.size(); i++) {
            PlayerTemplate playerTemplate = boardTemplate.players.get(i);
            Player newPlayer = new Player(result, playerTemplate.color, playerTemplate.name);
            result.addPlayer(newPlayer);

            newPlayer.setSpace(result.getSpace(playerTemplate.spaceX, playerTemplate.spaceY));
            newPlayer.heading = Heading.valueOf(playerTemplate.heading);
            newPlayer.checkPoints = playerTemplate.checkPoints;
            newPlayer.priority = playerTemplate.priority;


            CommandCardField[] newCards = new CommandCardField[playerTemplate.cards.length];
            CommandCardField[] newProgram = new CommandCardField[playerTemplate.program.length];

            // Loading players cards
            for (int j = 0; j < playerTemplate.cards.length; j++) {
                String commandName = playerTemplate.cards[j].card.command.type;
                if (commandName.equals("")) {
                    CommandCardField ccf = new CommandCardField(newPlayer);
                    newCards[j] = ccf;
                } else {
                    Command c = Command.valueOf(commandName);
                    CommandCard cc = new CommandCard(c);
                    CommandCardField ccf = new CommandCardField(newPlayer);
                    ccf.setCard(cc);
                    ccf.setVisible(playerTemplate.cards[j].visible);
                    newCards[j] = ccf;
                }
            }

            // loading players program
            for (int j = 0; j < playerTemplate.program.length; j++) {
                String commandName = playerTemplate.program[j].card.command.type;
                if (commandName.equals("")) {
                    CommandCardField ccf = new CommandCardField(newPlayer);
                    newProgram[j] = ccf;
                } else {
                    Command c = Command.valueOf(commandName);
                    CommandCard cc = new CommandCard(c);
                    CommandCardField ccf = new CommandCardField(newPlayer);
                    ccf.setCard(cc);
                    ccf.setVisible(playerTemplate.program[j].visible);
                    newProgram[j] = ccf;
                }
            }

            // Finish up
            newPlayer.cards = newCards;
            newPlayer.program = newProgram;
        }

        if (savedGame) {
            int currentPlayerIndex = boardTemplate.currentPlayer;
            result.setCurrentPlayer(result.getPlayer(currentPlayerIndex));
        }

        return result;
    }

    // player can load a board game
    public static Board loadBoardGame(String name) throws BoardDoesNotExistException {
        String resourcePath = SAVEDBOARDS + "/" + name + "." + JSONFile;
        String json = IOUtil.readGameJson(resourcePath);

        return deserialize(json, true);

    }


    public static Board newBoard(int numPlayers, String boardName) throws BoardDoesNotExistException {
        NewBoard = true;


        String resourcePath = BOARDS + "/" + boardName + "." + JSONFile;
        String json = IOUtil.readGameJson(resourcePath);

        Board board = deserialize(json, false);

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

    private static void PlayersPlace(List<Player> players, List<Space> possibleSpaces) {

        for (Player currentPlayer : players) {
            Space currentSpace = possibleSpaces.get(0);

            currentPlayer.setSpace(currentSpace);
            possibleSpaces.remove(currentSpace);

            currentPlayer.setHeading(Heading.EAST);
        }
    }

    // all spaces on the board get a Field action
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
