package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**

 */
public class RotatingGear extends FieldAction {
    public enum headingDirection {
        LEFT,
        RIGHT
    }

    private headingDirection direction;

    public void setheadingDirection(headingDirection direction) {
        this.direction = direction;
    }

    public headingDirection getDirection() {
        return direction;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        if (space.getActions().size() > 0) {
            RotatingGear gear = (RotatingGear) space.getActions().get(0);
            if (gear.getDirection() == headingDirection.LEFT) {
                gameController.turnLeft(space.getPlayer());
            } else {
                GameController.turnRight(space.getPlayer());
            }
        } else {
            return false;
        }

        return true;
    }
}
