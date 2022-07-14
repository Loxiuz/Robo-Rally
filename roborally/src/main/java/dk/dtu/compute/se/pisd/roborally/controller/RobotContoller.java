package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.PriorityAntenna;
import dk.dtu.compute.se.pisd.roborally.exceptions.MoveNotPossibleException;
import dk.dtu.compute.se.pisd.roborally.model.*;
import org.jetbrains.annotations.NotNull;

import static dk.dtu.compute.se.pisd.roborally.model.Command.AGAIN;

// this class use command card to Control the robot's move on the board
public class RobotContoller {

    static GameController gameController;


    public void RobotController(GameController gameController) {
        this.gameController = gameController;
    }

    // Control robot moves, robot move forward
    public static void moveForward(@NotNull Player player, int moves) {
        for (int i = 0; i < moves; i++) {
            try {
                Heading heading = player.getHeading();
                Space target = gameController.board.getNeighbour(player.getSpace(), heading);
                if (target == null ||
                        (target.getActions().size() > 0 && target.getActions().get(0) instanceof PriorityAntenna))
                    throw new MoveNotPossibleException(player, player.getSpace(), heading);
                if (isOccupied(target)) {
                    Player playerBlocking = target.getPlayer();
                    Heading targetCurrentHeading = playerBlocking.getHeading();
                    playerBlocking.setHeading(player.getHeading());
                    moveForward(playerBlocking, 1);
                    playerBlocking.setHeading(targetCurrentHeading);
                }
                target.setPlayer(player);
            } catch (MoveNotPossibleException e) {

            }
        }
    }



    // Control robot moves, robot move one step back
    public static void moveBackward(Player player) {
        uTurn(player);
        moveForward(player, 1);
        uTurn(player);
    }

    // Control robot moves, robot Movie again
    public static void again(Player player, int step) {
        if (step < 1) return;
        Command prevCommand = player.getProgramField(step - 1).getCard().command;
        if (prevCommand == AGAIN)
            again(player, step - 1);
        else {
            player.getProgramField(step).setCard(new CommandCard(prevCommand));
            gameController.executeNextStep();
            player.getProgramField(step).setCard(new CommandCard(AGAIN));
        }
    }

    // Control robot moves, robot turn Right
    public static void turnRight(@NotNull Player player) {
        if (player.board == gameController.board) {
            player.setHeading(player.getHeading().next());
        }
    }

    // Control robot moves, robot turn left
    public static void turnLeft(@NotNull Player player) {
        if (player.board == gameController.board) {
            player.setHeading(player.getHeading().prev());
        }
    }

    // Control robot moves, robot turn U
    public static void uTurn(Player player) {
        turnLeft(player);
        turnLeft(player);
    }
    private static boolean isOccupied(Space space) {
        Space target = gameController.board.getSpace(space.x, space.y);
        return target.getPlayer() != null;
    }

}
