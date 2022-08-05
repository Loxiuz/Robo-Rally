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

import dk.dtu.compute.se.pisd.httpclient.Client;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.*;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.exceptions.MoveNotPossibleException;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SerializeAndDeserialize;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.view.BoardView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dk.dtu.compute.se.pisd.roborally.model.Command.AGAIN;


/**
 * All the game logic for RoboRally.
 */
public class GameController {

    public static Board board;
    final private AppController appController;

    public final Client client;
    private boolean skipProgrammingPhase = true;
    private UpdateServerBoard updater;
    private int playerNumber;


    /**
     * Made as constructor.
     */
    public GameController(AppController appController, @NotNull Board board, Client client) {
        this.appController = appController;
        this.board = board;
        this.client = client;

        if (client != null) {
            client.updateServerGame(SerializeAndDeserialize.serialize(board));
            playerNumber = client.getRobotNumber();
            updater = new UpdateServerBoard();
            updater.setGameController(this);
            updater.setClient(client);
            updater.start();
        }

    }



    /**
     * Start the programming phase and clear all registers
     */
    public void startProgrammingPhase() {
        // All this should be done for the first reload for a newly constructed board
        boolean isNewlyLoadedDefaultBoard = SaveAndLoad.getNewBoardCreated();

        refreshUpdater();

        if (isNewlyLoadedDefaultBoard || !skipProgrammingPhase) {
            board.setPhase(Phase.PROGRAMMING);
            board.setCurrentPlayer(board.getPlayer(0));
            board.setStep(0);

            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                if (player != null) {
                    for (int j = 0; j < Player.NO_REGISTERS; j++) {
                        CommandCardField field = player.getProgramField(j);
                        field.setCard(null);
                        field.setVisible(true);
                    }

                    for (int j = 0; j < Player.NO_CARDS; j++) {
                        CommandCardField field = player.getCardField(j);
                        if (!player.getDmgcards().isEmpty()) {
                            if (player.getDmgcards().size() > j) {
                                field.setCard(new CommandCard(player.getDmgcards().get(j)));
                            } else
                                field.setCard(generateRandomCommandCard());
                        } else
                            field.setCard(generateRandomCommandCard());
                        field.setVisible(true);

                    }
                }
            }
        } else {
            skipProgrammingPhase = false;
        }
    }

    /**
     * Generate random command card
     * @return return the generated card.
     */
    public CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        ArrayList<Command> commandList = new ArrayList<>(Arrays.asList(commands).subList(0, 9));
        int random = (int) (Math.random() * commandList.size());
        return new CommandCard(commandList.get(random));
    }

    /**
     * Changes the phase from programming to activation.
     */
    public void finishProgrammingPhase() {
        if (board.getPlayerNumber(board.getCurrentPlayer()) == board.getPlayers().size() - 1 ||
                client == null) {
            makeProgramFieldsInvisible();
            makeProgramFieldsVisible(0);
            Player_ChangeBoardPlayers();


            board.setPhase(Phase.ACTIVATION);
            board.setCurrentPlayer(board.getPlayer(0));
            board.setStep(0);

            if (client != null) {
                refreshUpdater();
                pushGameSituation();
            }

        } else if (client != null) {
            changePlayer(board.getCurrentPlayer(), board.step);
        }
    }

    /**
     * Makes a card register visible
     * @param register the card register to make visible.
     */
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    /**
     * Show all players programming fields
     */
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    /**
     * Execute programs and change phase
     */
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    /**
     * Execute steps and continue game
     */
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    /**
     * Execute command before player change.
     * @param command Command to be executed.
     */
    public void execute_Command_Activation(Command command) {
        board.setPhase(Phase.ACTIVATION);

        Player currentPlayer = board.getCurrentPlayer();
        executeCommand(currentPlayer, command);
        changePlayer(currentPlayer, board.getStep());
    }

    /**
     * Create a new board view when board is changed
     */
    public void updateBoard() {
        appController.getRoboRally().createBoardView(this);
    }

    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    /**
     * Executes the next step in the players programming felt
     */
    protected void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if (board.getPhase() == Phase.ACTIVATION && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                CommandCard card = currentPlayer.getProgramField(step).getCard();
                if (card != null) {
                    Command command = card.command;
                    executeCommand(currentPlayer, command);
                }
                if (board.getPhase() == Phase.ACTIVATION) {
                    changePlayer(currentPlayer, step);
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    /**
     * Ends the current game and close the game
     */
    public void endGame() {
        Platform.runLater(appController::Client_Disconnect_Server);
        Platform.runLater(appController::stopGame);
    }

    public void Player_ChangeBoardPlayers() {
        Space antennaSpace = board.getPriorityAntennaSpace();

        if(antennaSpace.getActions().size() != 0){
            antennaSpace.getActions().get(0).doAction(this, antennaSpace);
        }

        // To avoid sync bug when playing online
        if (client == null) {
            List<Player> players = board.getPlayers();
            List<Integer> playersPriority = new ArrayList<>();

            // Get distance for each player to the antenna
            for (Player player : players) {
                Space playerSpace = player.getSpace();

                double totalDistance = Math.sqrt(Math.pow(Math.abs(playerSpace.x - antennaSpace.x), 2) + Math.pow(Math.abs(playerSpace.y - antennaSpace.y), 2));
                totalDistance = Math.round(totalDistance * 100); // To remove decimals
                playersPriority.add((int) totalDistance);
            }

            // Prioritize player according to their distance to the antenna.
            List<Player> prioritizedPlayers = new ArrayList<>();
            for (int i = 0; i <= (board.width + board.height) * 100; i++) {
                for (int j = 0; j < players.size(); j++) {
                    if (playersPriority.get(j) == i) {
                        prioritizedPlayers.add(players.get(j));
                    }
                }
            }

            board.setPlayers(prioritizedPlayers);
            board.setCurrentPlayer(prioritizedPlayers.get(0));

            if (appController != null)
                recreatePlayersView();
        }
    }

    /**
     * Change the player and the step of the board
     * @param currentPlayer from current player to next player
     * @param step Step of the register.
     */
    private void changePlayer(Player currentPlayer, int step) {
        int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
        if (nextPlayerNumber < board.getPlayersNumber()) {
            board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
        } else {
            step++;
            Activation_on_Board();
            if (step < Player.NO_REGISTERS) {
                makeProgramFieldsVisible(step);
                board.setStep(step);
                board.setCurrentPlayer(board.getPlayer(0));
            } else {
                startProgrammingPhase();
            }
        }
        pushGameSituation();
        refreshUpdater();
    }

    /**
     * Move current player to a certain space.
     * @param space Space to be moved to.
     * @author Malte B.
     */
    public void moveCurrentPlayerToSpace(@NotNull Space space) {
        if (space.board == board) {
            Player currentPlayer = board.getCurrentPlayer();
            if (currentPlayer != null && space.getPlayer() == null) {
                currentPlayer.setSpace(space);

                if (space.getActions().size() > 0) {
                    FieldAction action = space.getActions().get(0);
                    action.doAction(this, space);
                }

                int playerNumber = (board.getPlayerNumber(currentPlayer) + 1) % board.getPlayersNumber();
                board.setCurrentPlayer(board.getPlayer(playerNumber));
            }
        }
    }

    /**
     * A player executing a command.
     * @param player The player executing the command.
     * @param command the command to be executed.
     * @author Malte B.
     */
    private void executeCommand(@NotNull Player player, Command command) {
        if (player.board == board && command != null) {
            switch (command) {
                case MOVE1 -> moveForward(player, 1);
                case MOVE2 -> moveForward(player, 2);
                case MOVE3, SPEEDROUTINE -> moveForward(player, 3);
                case RIGHT -> turnRight(player);
                case LEFT -> turnLeft(player);
                case MOVEBACK -> moveBackward(player);
                case AGAIN, REPEATROUTINE -> again(player, board.getStep());
                case SPAM -> removeSpamCard(player);
                case OPTION_LEFT_RIGHT, SANDBOXROUTINE, WEASELROUTINE -> board.setPhase(Phase.PLAYER_INTERACTION);
                case UTURN -> uTurn(player);

                default -> {
                }

            }
            if (client != null)
                client.updateServerGame(SerializeAndDeserialize.serialize(board));
        }
    }

    /**
     * Moves one command card to a different field.
     * @param source The command card field the card is taken from.
     * @param target The command card field the card is taken to.
     * @return Conformation whether valid or not.
     */
    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves player's robot an amount spaces forward.
     * @param player Player/robot to be moved.
     * @param moves Number of moves.
     * @author Malte B.
     */
    public static void moveForward(@NotNull Player player, int moves) {
        for (int i = 0; i < moves; i++) {
            try {
                Heading heading = player.getHeading();
                Space target = board.getNeighbour(player.getSpace(), heading);
                if (target == null ||
                        (target.getActions().size() > 0 && target.getActions().get(0) instanceof PriorityAntenna))
                    throw new MoveNotPossibleException(player, player.getSpace(), heading);
                if (isOccupied(target)) {
                    Player playerBlocking = target.getPlayer();
                    playerBlocking.setSpace(board.getNeighbour(target,heading));
                }
                target.setPlayer(player);
            } catch (MoveNotPossibleException e) {

            }
        }
    }

    /**
     * Turns player's robot 90 degrees right.
     * @param player Player turning.
     * @author Malte B.
     */
    public static void turnRight(@NotNull Player player) {
        if (player.board == board) {
            player.setHeading(player.getHeading().next());
        }
    }

    /**
     * Turns player's robot degrees left.
     * @param player Player turning.
     * @author Malte B.
     */
    public static void turnLeft(@NotNull Player player) {
        if (player.board == board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    /**
     * Turns player's robot 180 degrees.
     * @param player Player turning.
     * @author Malte B.
     */
    public static void uTurn(Player player) {
        turnLeft(player);
        turnLeft(player);
    }

    /**
     * Moves player backwards.
     * @param player Player to be moved.
     * @author Malte B.
     */
    public static void moveBackward(Player player) {
        uTurn(player);
        moveForward(player, 1);
        uTurn(player);
    }

    /**
     * Executes previous command again.
     * @param player Effected player.
     * @param step Step of the current register.
     * @author Christian B.
     */
    public void again(Player player, int step) {
        if (step < 1) return;
        Command prevCommand = player.getProgramField(step - 1).getCard().command;
        if (prevCommand == AGAIN)
            again(player, step - 1);
        else {
            player.getProgramField(step).setCard(new CommandCard(prevCommand));
            executeNextStep();
            player.getProgramField(step).setCard(new CommandCard(AGAIN));
        }
    }

    /**
     * Checks if a space is occupied by a player.
     * @param space Space to check.
     * @author Christian B.
     */
    private static boolean isOccupied(Space space) {
        Space target = board.getSpace(space.x, space.y);
        return target.getPlayer() != null;
    }

    public void recreatePlayersView() {
        BoardView boardView = appController.getRoboRally().getBoardView();
        boardView.updatePlayersView();
    }


    /**
     * Controls robot activation on the board
     * @author Malte B.
     */
    private void Activation_on_Board() {
        List<Player> players = board.getPlayers();
        ArrayDeque<Player> actionsToBeHandled = new ArrayDeque<>(board.getPlayersNumber());

        for (int i = 2; i > 0; i--) {
            for (Player player : players) {
                if (!player.getSpace().getActions().isEmpty() &&
                        player.getSpace().getActions().get(0) instanceof ConveyorBelt spaceBelt &&
                        (spaceBelt.getNumberOfMoves() == i)) { //check if the space have an action
                    actionsToBeHandled.add(player);
                }
            }
            int playersInQueue = actionsToBeHandled.size();
            int j = 0;
            while (!actionsToBeHandled.isEmpty()) {
                Player currentPlayer = actionsToBeHandled.pop();
                Space startLocation = currentPlayer.getSpace();
                if (!currentPlayer.getSpace().getActions().get(0).doAction(this, currentPlayer.getSpace())) {
                    currentPlayer.setSpace(startLocation);
                    actionsToBeHandled.add(currentPlayer);
                }
                j++;
                if (j == playersInQueue)
                    if (playersInQueue == actionsToBeHandled.size()) {
                        actionsToBeHandled.clear();
                        break;
                    } else {
                        j = 0;
                        playersInQueue = actionsToBeHandled.size();
                    }
            }
        }

        //activate PushPanel
        for (Player player : players) {
            if (!player.getSpace().getActions().isEmpty() &&
                    player.getSpace().getActions().get(0) instanceof PushPanel)
                player.getSpace().getActions().get(0).doAction(this, player.getSpace());
        }

        //activate gears
        for (Player player : players) {
            if (!player.getSpace().getActions().isEmpty() &&
                    player.getSpace().getActions().get(0) instanceof RotatingGear)
                player.getSpace().getActions().get(0).doAction(this, player.getSpace());
        }


        //activate RebootToken
        for (Player player : players) {
            if (!player.getSpace().getActions().isEmpty() &&
                    player.getSpace().getActions().get(0) instanceof RebootToken)
                player.getSpace().getActions().get(0).doAction(this, player.getSpace());
        }
        //activate Pit
        for (Player player : players) {
            if (!player.getSpace().getActions().isEmpty() &&
                    player.getSpace().getActions().get(0) instanceof Pit)
                player.getSpace().getActions().get(0).doAction(this, player.getSpace());
        }


        //activate checkpoints
        for (Player player : players) {
            if (!player.getSpace().getActions().isEmpty() &&
                    player.getSpace().getActions().get(0) instanceof Checkpoint)
                player.getSpace().getActions().get(0).doAction(this, player.getSpace());
        }
    }

    /**
     * Updates the board when a non-active player are pulling
     * and players taking their turn are not pulling.
     */
    public void refreshUpdater() {
        if (client != null) {
            updater.setUpdate(isMyTurn());
            if (board.gameOver) endGame(); // Needed to ensure it closes
        }
        if (board.gameOver)
                updater.setUpdate(false);
        }


    /**
     * Pushes the current game state to the connected server id
     */
    public void pushGameSituation() {
        if (client != null)
            client.updateServerGame(SerializeAndDeserialize.serialize(board));
    }

    /**
     * Checks if a player is connected to an online game has his/hers turn.
     * This is determined by their id given from the server
     * @return
     */
    public boolean isMyTurn() {
        return board.getCurrentPlayer() != board.getPlayer(playerNumber) && client != null;

    }

    public void setPlayerNumber(int number) {
        playerNumber= number;
    }


    public int getPlayerNumber() {
        return playerNumber;
    }


    private void removeSpamCard(Player player) {
        player.getDmgcards().remove(Command.SPAM);
    }

    //

    /**
     * The winner gets a massage when a player got all the checkpoints on the board in the correct order.
     * @param space
     */
    public void Winner_Massage(Space space){
        Alert winnerMassage = new Alert(Alert.AlertType.INFORMATION);
        winnerMassage.setTitle("Game Ended");
        winnerMassage.setHeaderText("The game has ended. " + " The winner is: " + space.getPlayer().getName());
        winnerMassage.showAndWait();

    }

}
