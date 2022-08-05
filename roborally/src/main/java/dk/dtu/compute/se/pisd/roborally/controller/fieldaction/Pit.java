package dk.dtu.compute.se.pisd.roborally.controller.fieldaction;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Command;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;

/**

 */
public class Pit extends FieldAction {
    public int boardNum;
    RebootToken reboottoken = new RebootToken();
    @Override
    public boolean doAction(GameController gameController, Space space) {
        Pit pitAction = (Pit) space.getActions().get(0);
        Board board = gameController.board;

        if (space.getActions().size() > 0) {
            Player player = space.getPlayer();

            if (player != null) {
                switch (pitAction.boardNum) {
                    case 1:

                        board.getSpace(7,0).setPlayer(player);
                        reboottoken.doAction(gameController, player.getSpace());
                        player.setDamagecards(Command.SPAMDamge);
                        player.setDamagecards(Command.SPAMDamge);
                        break;

                    case 2:
                        board.getSpace(4,0).setPlayer(player);
                        reboottoken.doAction(gameController, player.getSpace());
                        player.setDamagecards(Command.SPAMDamge);
                        player.setDamagecards(Command.SPAMDamge);
                        break;

                }
            }

        }
        return true;
    }
}
