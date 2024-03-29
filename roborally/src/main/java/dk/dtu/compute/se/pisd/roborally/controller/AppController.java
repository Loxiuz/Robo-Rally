/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.httpclient.Client;
import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.exceptions.BoardDoesNotExistException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.IOUtil;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SaveAndLoad;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controls the application before the game is started.
 */
public class AppController implements Observer {
    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private RoboRally roboRally;
    private GameController gameController;
    private final Client client = new Client();
    private boolean server_Client = false;


    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    /**
     * Used by the new game menu option to start a new game.
     */
    public void newGame() {
        Optional<Integer> numPlayers = AppController.ChooseNumberOfPlayers(PLAYER_NUMBER_OPTIONS);

        if (numPlayers.isPresent()) {
            if (gameController != null && !stopGame()) return;
            createNewGame(numPlayers.get(), false);
        }
    }


    /**
     * Creates a new game and shows the game
     * @param numberofPlayers Number of players.
     * @param prevFailed
     * @author Elias R.
     */
    private void createNewGame(int numberofPlayers, boolean prevFailed) {
        //Optional<String> chosenBoard = askUserWhichDefaultBoard(prevFailed);

        Optional<String> chosenBoardGame = AppController.ChooseBoardGame(IOUtil.getBoardGameName());
        if (chosenBoardGame.isPresent()) {
            try {
                Board board = SaveAndLoad.newBoard(numberofPlayers, chosenBoardGame.get());
                setupGameController(board);
                if (client.isClientOnServer())
                    client.updateGameSituation(SaveAndLoad.serialize(board));
            } catch (BoardDoesNotExistException e) {
                createNewGame(numberofPlayers, true);
            }
        }
    }

    /**
     * Allows players to write a text dialog.
     * @param input Input from player in an array.
     * @return Returns null.
     * @author Peter J.
     */
    public static String ServerDialog(String[] input) {
        TextInputDialog ServerDialog = new TextInputDialog();
        ServerDialog.setTitle(input[0]);
        ServerDialog.setHeaderText(input[1]);
        Optional<String> choise = ServerDialog.showAndWait();
        if (!choise.isEmpty())
            return choise.get();
        return null;
    }

    //

    /**
     * Asks users for number of players in a dialog box.
     * @param list List of different amounts.
     * @return Returns dialog and waits for input.
     */
    public static Optional<Integer> ChooseNumberOfPlayers(List<Integer> list) {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");

        return dialog.showAndWait();
    }

    //

    /**
     * Asks players to choose a game board.
     * @param list List of saved boards.
     * @return Returns dialog and waits for input.
     */
    public static Optional<String> ChooseBoardGame(List<String> list) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle("CHOOSE BOARD");
        dialog.setHeaderText("Select a board to play");

        return dialog.showAndWait();
    }

    /**
     *
     * @param list
     * @return
     * @author Peter J.
     */
    public static Optional<String> loadBoardName(List<String> list) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(list.get(0), list);
        dialog.setTitle("Load Game");
        dialog.setHeaderText("load a game to play");

        return dialog.showAndWait();
    }
    /**
     * Gives a warning.
     * @param input A string to be shown as a warning
     * @return Returns and waits.
     */
    public static Optional<ButtonType> warningDialog(String[] input) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(input[0]);
        alert.setContentText(input[1]);
        return alert.showAndWait();
    }

    /**
     * Save game Stage and let a player write a name and  save a game
     * @author Elias R.
     */
    public void saveGame() {
            String[] s = new String[]{"SAVE YOUR GAME", "Save your game"};
            String SaveGamedialog = AppController.ServerDialog(s);

            if (SaveGamedialog != null)
                SaveAndLoad.SaveBoardGame(gameController.board, SaveGamedialog);
        }

    //

    /**
     * Player can load a game
     * @author Elias R.
     */
    private void LoadGameBoard() {
        Optional<String> choseeBoardGame = AppController.loadBoardName(IOUtil.getSavedBoardsName());
        if (choseeBoardGame.isPresent()) {
            try {
                if ("Name".equals(choseeBoardGame.get())) {
                    System.out.println("Is the same");
                }

                Board board = SaveAndLoad.loadBoardGame(choseeBoardGame.get());
                setupGameController(board);
            } catch (BoardDoesNotExistException e) {
                LoadGameBoard();
            }
        }
    }

    public void loadGame() {
        if (gameController == null) {
            LoadGameBoard();
        }
    }


    /**
     * Start a new game and shows the Gui and sets up game controller.
     * @param board Selected board to be used.
     */
    private void setupGameController(Board board) {
        gameController = new GameController(this, Objects.requireNonNull(board), server_Client ? client : null);
        board.setCurrentPlayer(board.getPlayer(0));
        gameController.startProgrammingPhase();
        roboRally.createBoardView(gameController);
    }


    public boolean stopGame() {
        if (gameController != null) {

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    /**
     * Shows a window and ask player to exit the game or not
     */
    public void exitGame() {
        if (gameController != null) {
            String[] s = new String[]{"Exit RoboRally?", "Are you sure you want to exit RoboRally?"};
            Optional<ButtonType> result = AppController.warningDialog(s);

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }

        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null || stopGame()) {
            client.leaveServer();
            Platform.exit();
        }
    }

    /**
     * Player disconnects from the server
     * @author Elias R.
     */
    public void ServerDisconnection() {
        client.leaveServer();
    }

    /**
     * Checks if the game is running or not
     * @return Returns true if game is running.
     */
    public boolean isGamepresent() {
        return gameController != null;
    }

    @Override
    public void update(Subject subject) {

    }

    public RoboRally getRoboRally() {
        return roboRally;
    }

    /**
     * Hosts game on the server and starts the game
     * @param errorMessage the relevant message
     * @author Elias R.
     */
    public void ClientHostGame(String... ResponseMassage) {
        String[] HostGameDialog = new String[]{"Multiplayer game ", "Write your server Name:"};
        if (ResponseMassage.length != 0)
            HostGameDialog[1] = ResponseMassage[0] + "\n Try again";
        String state = AppController.ServerDialog(HostGameDialog);
        if (state == null)
            return;
        String Serverresponse = client.hostServerGame(state);
        if (!Objects.equals(Serverresponse, "success"))
            ClientHostGame(Serverresponse);
        else {
            server_Client = true;
            newGame();
        }
    }

    /**
     * Player chooses a server from server table and joins the game
     * @param id the chosen server's ID
     * @author Elias R.
     */
    public void ClientJoinGame(String id) {
        String ResponseMessage = client.joinToAGame(id);
        if (ResponseMessage.equals("ok")) {
            server_Client = true;
            Board board = SaveAndLoad.deserialize(client.getGameSituation(), true);
            setupGameController(board);
            gameController.setPlayerNumber(client.getRobotNumber());

        }
    }

    /**
     * Player can see the available servers on the server table
     * @author Elias R.
     */
    public void ConnectClientToServer() {
        String ListOfServer = client.listGamesOnServer(); //gets the list of servers in the table
        if (ListOfServer.equals("server timeout")) { //Give a massage to player if server is not reachable
            AppController.warningDialog(new String[]{"error", ListOfServer, "try again"});
            return;
        }
        RoboRally.addClientOnServer(ListOfServer); //adds the servers to the view

    }
}
