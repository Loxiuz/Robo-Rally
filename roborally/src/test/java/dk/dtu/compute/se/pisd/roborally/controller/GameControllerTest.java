package java.dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.controller.SaveAndLoad;
import dk.dtu.compute.se.pisd.roborally.exceptions.BoardDoesNotExistException;
import dk.dtu.compute.se.pisd.roborally.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameControllerTest {

    private final int TEST_WIDTH = 8;
    private final int TEST_HEIGHT = 8;

    private GameController gameController;
    @BeforeEach
    void setUp() {
        gameController = new GameController(
                null, new Board(TEST_WIDTH, TEST_HEIGHT), null);
        Board board = gameController.board;
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null,"Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {
        gameController = null;
    }

    @Test
    void startProgrammingPhase(){
        Board board = gameController.board;
        board.setPhase(Phase.INITIALISATION);
        gameController.startProgrammingPhase();

        Phase expected = Phase.PROGRAMMING;
        Phase actual = board.getPhase();

        Assertions.assertEquals(expected, actual,
                "The active phase should be the programming phase.");
    }
    @Test
    void finishProgrammingPhase(){
        Board board = gameController.board;
        board.setPhase(Phase.PROGRAMMING);
        gameController.finishProgrammingPhase();

        Phase expected = Phase.ACTIVATION;
        Phase actual = board.getPhase();

        Assertions.assertEquals(expected, actual,
                "The programming phase should end and shift to activation phase.");
    }

    @Test
    void generateRandomCommandCard(){
        Command[] commands = Command.values();
        Command expected = null;
        Command actual = gameController.generateRandomCommandCard().command;
        for (Command command : commands) {
            expected = command;
            if (actual == expected) {
                break;
            }
        }
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void moveCurrentPlayerToSpace() {
        Board board = gameController.board;
        Player player1 = board.getPlayer(0);
        Player player2 = board.getPlayer(1);

        gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));

        Assertions.assertEquals(player1, board.getSpace(0, 4).getPlayer(), "Player " + player1.getName() + " should beSpace (0,4)!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
        Assertions.assertEquals(player2, board.getCurrentPlayer(), "Current player should be " + player2.getName() +"!");
    }

    @Test
    void moveForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.moveForward(current, 1);

        Assertions.assertEquals(current, board.getSpace(0, 1).getPlayer(), "Player " + current.getName() + " should beSpace (0,1)!");
        Assertions.assertEquals(Heading.SOUTH, current.getHeading(), "Player 0 should be heading SOUTH!");
        Assertions.assertNull(board.getSpace(0, 0).getPlayer(), "Space (0,0) should be empty!");
    }

    @Test
    void fastForward() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();
    }

    @Test
    void turnRight() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.turnRight(current);

        Assertions.assertEquals(Heading.WEST, current.getHeading(),
                "Player " + current.getName() + " should be heading west!");
    }

    @Test
    void turnLeft() {
        Board board = gameController.board;
        Player current = board.getCurrentPlayer();

        gameController.turnLeft(current);

        Assertions.assertEquals(Heading.EAST, current.getHeading(),
                "Player " + current.getName() + " should be heading East!");
    }


    @Test
    void testCheckPoints() {
        Board board = null;
        try {
            board = SaveAndLoad.newBoard(2, "SprintCramp");
            GameController gameController = new GameController(null, board, null);

            board.setCurrentPlayer(board.getPlayers().get(0));
            gameController.moveCurrentPlayerToSpace(board.getSpace(12, 8));
            board.setCurrentPlayer(board.getPlayers().get(0));
            gameController.moveCurrentPlayerToSpace(board.getSpace(5, 2));
            board.setCurrentPlayer(board.getPlayers().get(0));
            gameController.moveCurrentPlayerToSpace(board.getSpace(4, 9)); // Will cause exception

        } catch (BoardDoesNotExistException e) {
            Assertions.fail(); // Board not found
        } catch (ExceptionInInitializerError e2) {
            Assertions.assertEquals(3, board.getCurrentPlayer().checkPoints);
        }
    }
}