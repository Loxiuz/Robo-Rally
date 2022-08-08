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
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.*;
import dk.dtu.compute.se.pisd.roborally.controller.fieldaction.Checkpoint;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/*  this class shows  different elements on the board game
    like background, conveyorbelts, Laser, Pushpanel, Pit , wall and check points
    and update player and view for users
*/

public class SpaceView extends StackPane implements ViewObserver {
/*
    final public static int SPACE_HEIGHT = 40; // 60; // 75;
    final public static int SPACE_WIDTH = 40;  // 60; // 75;
*/
    public Space spaceview;

    //space view constructor with parameter space

    public SpaceView(@NotNull Space space) {

        this.spaceview = space;
        this.setId("space");
        this.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/Images/RoboRally_Stylesheet.css")).toExternalForm());

        // background's board
        ImageView background = new ImageView(new Image("Images/background.png"));
        this.getChildren().add(background);

        //conveyorBelt
        if (space.getActions().size() > 0) {
            ImageView SpaceView;
            if (space.getActions().get(0) instanceof ConveyorBelt conveyorBelt) {
                Image conveyorBelts;
                if (conveyorBelt.getNumberOfMoves() <= 1) {
                    conveyorBelts = new Image("Images/conveyorBelt.png");
                } else {
                    conveyorBelts = new Image("Images/conveyorBeltBlue.png");
                }
                SpaceView = new ImageView(conveyorBelts);
                SpaceView.setRotate((90 * conveyorBelt.getHeading().ordinal()) % 360);
                this.getChildren().add(SpaceView);

                //PushPanels
            } else if (space.getActions().get(0) instanceof PushPanel pushPanel) {
                SpaceView = new ImageView(new Image("Images/pushPanel.png"));
                SpaceView.setRotate((90 * pushPanel.getHeading().ordinal()) % 360);
                this.getChildren().add(SpaceView);

                //Energy
            } else if (space.getActions().get(0) instanceof Energy) {
                SpaceView = new ImageView(new Image("Images/energy.png"));
                this.getChildren().add(SpaceView);

                //Rotating Gear Right and Left
            } else if (space.getActions().get(0) instanceof RotatingGear rotatingGear) {
                if (rotatingGear.getDirection() == RotatingGear.moveDirection.RIGHT) {
                    SpaceView = new ImageView(new Image("Images/rotatingGearRight.png"));
                } else {
                    SpaceView = new ImageView(new Image("Images/rotatingGearLeft.png"));
                }
                this.getChildren().add(SpaceView);

                //Pit
            } else if (space.getActions().get(0) instanceof Pit) {
                SpaceView = new ImageView(new Image("Images/pit.png"));
                this.getChildren().add(SpaceView);

                //numbers of Check Points
            } else if (space.getActions().get(0) instanceof Checkpoint checkpoint) {
                switch (checkpoint.getCheckpointNumber()) {
                    case 1 -> SpaceView = new ImageView(new Image("Images/checkPoint1.png"));
                    case 2 -> SpaceView = new ImageView(new Image("Images/checkPoint2.png"));
                    case 3 -> SpaceView = new ImageView(new Image("Images/checkPoint3.png"));
                    default -> SpaceView = new ImageView(new Image("Images/background.png"));
                }
                this.getChildren().add(SpaceView);

                //Antenna
            } else if (space.getActions().get(0) instanceof PriorityAntenna) {
                SpaceView = new ImageView(new Image("Images/priorityAntenna.png"));
                this.getChildren().add(SpaceView);

                //Starting Gear
            } else if (space.getActions().get(0) instanceof StartGear) {
                SpaceView = new ImageView(new Image("Images/startinggear.png"));
                this.getChildren().add(SpaceView);

                //Reboot
            } else if (space.getActions().get(0) instanceof RebootToken) {
                SpaceView = new ImageView(new Image("Images/reboot.png"));
                this.getChildren().add(SpaceView);
            }

        }

            //Walls
        for (Heading wall : space.getWalls()) {
            ImageView walls = new ImageView(new Image("Images/wall.png"));
            walls.setRotate((90 * wall.ordinal()) % 360);
            this.getChildren().add(walls);
        }


        space.attach(this);
        update(space);
    }
/*
                // XXX the following styling should better be done with styles
                this.setPrefWidth(SPACE_WIDTH);
                this.setMinWidth(SPACE_WIDTH);
                this.setMaxWidth(SPACE_WIDTH);

                this.setPrefHeight(SPACE_HEIGHT);
                this.setMinHeight(SPACE_HEIGHT);
                this.setMaxHeight(SPACE_HEIGHT);

                if ((space.x + space.y) % 2 == 0) {
                    this.setStyle("-fx-background-color: white;");
                  }
                else {
                    this.setStyle("-fx-background-color: black;");
                }


        // This space view should listen to changes of the space
        space.attach(this);
        update(this.space);

    }*/

    private void updatePlayer() {
        // this.getChildren().clear();

        for (int i = 0; i < this.getChildren().size(); i++) {
            if (this.getChildren().get(i).getClass().getSimpleName().equals("Polygon")) {
                this.getChildren().remove(i);
            }
        }
        Player player = spaceview.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 20,
                    20.0, 20.0,
                    10.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }


    }

    @Override
    public void updateView(Subject subject) {

        if (subject == this.spaceview) {
            updatePlayer();
        }
    }
}
/*
        boolean[] hasWall = new boolean[]{
                space.x == 3 && space.y == 4,
                space.x == 4 && space.y == 5,
                space.x == 1 && space.y == 2,
                space.x == 1 && space.y == 6,
                space.x == 6 && space.y == 3};
        for(int i = 0; i < hasWall.length; i++){

            int k = i;
            if(i > 3){k = 0;}
            if(hasWall[i]){
                Canvas canvas =
                        new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
                GraphicsContext gc =
                        canvas.getGraphicsContext2D();
                switch (k) {
                    case 0 -> { //NORTH
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(5);
                        gc.setLineCap(StrokeLineCap.ROUND);
                        gc.strokeLine(SPACE_WIDTH - 2, 2, 2, 2);
                    }
                    case 1 -> { //EAST
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(5);
                        gc.setLineCap(StrokeLineCap.ROUND);
                        gc.strokeLine(SPACE_WIDTH - 2, SPACE_HEIGHT - 2, SPACE_WIDTH - 2, 2);
                    }
                    case 2 -> { //SOUTH
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(5);
                        gc.setLineCap(StrokeLineCap.ROUND);
                        gc.strokeLine(2, SPACE_HEIGHT - 2, SPACE_WIDTH - 2, SPACE_HEIGHT - 2);
                    }
                    case 3 -> { //WEST
                        gc.setStroke(Color.RED);
                        gc.setLineWidth(5);
                        gc.setLineCap(StrokeLineCap.ROUND);
                        gc.strokeLine(2, SPACE_HEIGHT - 2, 2, 2);
                    }
                }
                this.getChildren().add(canvas);

            }
        }

        Canvas canvas = new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        boolean[] hasConveyorBelt = new boolean[]{
                space.x == 5 && space.y == 1,
                space.x == 5 && space.y == 2,
                space.x == 5 && space.y == 3,
                space.x == 2 && space.y == 4,
                space.x == 2 && space.y == 5,
                space.x == 2 && space.y == 6};
        for (boolean b : hasConveyorBelt) {
            if (b) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(3);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(20, 20, 20, 2);

                Polygon arrow = new Polygon(0, 0,
                        5, 10,
                        10, 0);
                try {
                    arrow.setFill(Color.BLUE);
                } catch (Exception e) {
                    arrow.setFill(Color.MEDIUMPURPLE);
                }
                this.getChildren().add(arrow);
                this.getChildren().add(canvas);
            }
        }

    }
*/




