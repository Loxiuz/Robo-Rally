package dk.dtu.compute.se.pisd.httpclient;

import dk.dtu.compute.se.pisd.roborally.controller.GameController;
import dk.dtu.compute.se.pisd.roborally.fileaccess.SaveAndLoad;
import javafx.application.Platform;


/**
 * This class update the board on the server when the game state is received it
 * and deserializes the json file and sends an async message to update the ui.
 */

public class UpdateServerBoard extends Thread {
    GameController gameController;
    Client client;
    boolean updateServer = true;
    boolean runable = true;

    public void run() {
        while (runable) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (updateServer) {
                gameController.NewUpdater();
                updateBoarOnServer();
            }
        }
    }

    public void updateBoarOnServer() {
        if (!gameController.board.gameOver) {
            gameController.board = SaveAndLoad.deserialize(client.getGameSituation(), true);
            Platform.runLater(gameController::updateBoard);
        }
    }



    public void setRun(boolean run) {
        this.runable= run;
    }
    public void setClient(Client client) {
        this.client = client;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public boolean getUpdate() {
        return updateServer;
    }
    public void setUpdate(boolean update) {
        this.updateServer = update;
    }
}
