package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.controller.RobotContoller;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

import java.nio.ReadOnlyBufferException;

/**

 */
public class RebootToken extends FieldAction {
    @Override
    public boolean doAction(GameController gameController, Space space) {
        if (space.getActions().size() > 0) {
            Player player = space.getPlayer();

            if (player != null) {
                RobotContoller.moveForward(player, 1);
            }

        }
        return false;
    }
}
