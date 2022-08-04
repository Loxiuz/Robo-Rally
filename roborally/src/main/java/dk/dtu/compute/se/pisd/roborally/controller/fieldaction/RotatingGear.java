package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**

 */
public class RotatingGear extends FieldAction {

    private moveDirection direction;

    public moveDirection getDirection() {
        return direction;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        if (space.getActions().size() > 0) {
            RotatingGear gear = (RotatingGear) space.getActions().get(0);
            if (gear.getDirection() == moveDirection.LEFT) {
                gameController.turnLeft(space.getPlayer());
            } else {
                GameController.turnRight(space.getPlayer());
            }
        } else {
            return false;
        }

        return true;
    }

    public enum moveDirection {
        LEFT,
        RIGHT
    }

}
