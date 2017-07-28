package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

import java.util.Iterator;

public class Main extends Application {

    private TextField scoreTF;
    private double interval; //milliseconds
    private boolean play;
    private Score score;
    private SecondWindow SW;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //set initial values
        interval = 10000;
        play = false;
        score = new Score();
        //create main window
        buildMainStage(primaryStage);
        primaryStage.show();
        //adjust the position and dimentions of the main stage to occupy the entirety of the screen it spawned in.
        setStageToFillScreen(primaryStage);
    }

    public void buildMainStage(Stage aStage)
    {

        aStage.setTitle("Catch me if you can!");
        Button startGiveUp = new Button("Start");
        startGiveUp.setStyle("-fx-font: 30 arial;");//easy way to make the button a little bigger
        //provide the plaer with some instructions
        Label instructions = new Label(
                "The objective of this game\n"
                + "is to click on the 'Click Me!'\n"
                + "button contained in a second\n"
                + "popup window that will periodically\n"
                + "move around the screen get faster\n"
                + "faster every time you land a click.\n"
                + "You get one point for every click.\n\n"
                + "Good luck!\n"
        );
        Label scoreLbl = new Label("Current Score");
        scoreTF = new TextField("0");
        scoreTF.setDisable(true);
        //place scoreLbl and scoreTF in a HBox so that that are placed next to each other
        HBox scoreVBox = new HBox(scoreLbl,scoreTF);
        //place the other controls and the scoreVBox in a VBox to lign them up on top of one another
        VBox root = new VBox(instructions,startGiveUp,scoreVBox);

        //set the alignments of the two containers so that out controllers are slightly more elegantly centered
        root.setStyle("-fx-alignment: center;");
        scoreVBox.setStyle("-fx-alignment: center;");
        aStage.setScene(new Scene(root));

        //assign what the start button dose
        //good example of the theading that goes on behind the scenes
        //the first time the button is pressed play == false so the movable window is started up and then
        //we enter a seemingly infinite loop because we never set play to false anywhere in the while loop
        //what actually happens is that when the players hits the button again a new thread is started that sets
        //play to false and hence leads to the other threads while loop ending
        startGiveUp.setOnAction(e ->
        {
            if (play)
            {
                play = false;
                startGiveUp.setText("Start");
                interval = 10000;
                SW.close();
            }
            else {
                play = true;
                startGiveUp.setText("Give Up :(");
                score.reset();
                while (play & aStage.isShowing()) {
                    SW = new SecondWindow(interval, aStage); // create new moving window
                    Stage SWwindow = SW.getStage(); // get the stage from moving window
                    SWwindow.showAndWait(); // suspend this function until the moving window is closed (by clicking 'click me!')
                    interval *= 0.9; // make the interval 10% faster
                    score.incrementScore(); // give the player a point
                }
            }
        });

    }

    public class Score
    {
        private int score;

        public Score()
        {
            score = 0;
        }

        public void reset()
        {
            score = 0;
            scoreTF.setText("" + score);
        }

        public void incrementScore()
        {
            score++;
            scoreTF.setText("" + score);
        }
    }

    private void setStageToFillScreen(Stage aStage)
    {
        final Iterator<Screen> iterator = Screen.getScreensForRectangle(aStage.getX(), aStage.getY(), aStage.getWidth(), aStage.getHeight()).iterator();
        Rectangle2D primaryScreenBounds = iterator.next().getVisualBounds();
        aStage.setX(0);
        aStage.setY(0);
        aStage.setWidth(primaryScreenBounds.getWidth());
        aStage.setHeight(primaryScreenBounds.getHeight());
    }

    public class SecondWindow
    {
        //object for managing the second stage
        Stage movingWindow;
        Button clickMe;
        double interval;
        Timeline moveTimer;

        public SecondWindow(double millisecondInterval, Stage parent)
        {
            interval = millisecondInterval;
            clickMe = new Button("ClickMe!");
            clickMe.setStyle("-fx-font: 24 arial;");//easy way to make the button a little bigger

            movingWindow = new Stage();
            movingWindow.setScene(new Scene(clickMe));
            movingWindow.initModality(Modality.NONE); //so that we can still click the give up button;
            movingWindow.initStyle(StageStyle.UNDECORATED); // so that the floating window has no title bar, just the button
            movingWindow.initOwner(parent);

            //all the click me button dose is close the window
            clickMe.setOnAction(e ->
            {
                movingWindow.close();
            });

            //randomise initial window location
            setRandomPosition(parent);
            //set up an infinite interval timer to continue to randomise window location based on interval time
            moveTimer = new Timeline(new KeyFrame(
                    Duration.millis(millisecondInterval),
                    e ->
                    {
                        setRandomPosition(parent);  // do this every millisecondInterval milliseconds
                    }
            ));
            moveTimer.setCycleCount(Animation.INDEFINITE); // do it forever (at least until stop() is called)
            moveTimer.play(); //start the interval timer
        }

        private void setRandomPosition(Stage parent)
        {
            //set the position of the moving window to a new random location anywhere not close the to give up button
            //in the center of the main window.
            final Iterator<Screen> iterator = Screen.getScreensForRectangle(parent.getX(), parent.getY(), parent.getWidth(), parent.getHeight()).iterator();
            Rectangle2D primaryScreenBounds = iterator.next().getVisualBounds();
            //note: don't get placed in the middle of the screen because this can make the player accidentally press give up
            double sizeOfCenterExclusionZone = 300;
            double screenWidthLessExclusionZone = primaryScreenBounds.getWidth()-sizeOfCenterExclusionZone;
            double screenHeightLessExclusionZone = primaryScreenBounds.getHeight()-sizeOfCenterExclusionZone;
            int x = (int)(Math.random()*(screenWidthLessExclusionZone-120));//also deduct width and height of button
            int y = (int)(Math.random()*(screenHeightLessExclusionZone-80));// so that it dose not move partially off screen
            //screenWidth/heightLessExclusionZone divided by 2 is the lower bound of the exclusion zone
            //so any values heigher than this need to have sizeOfCenterExclusionZone added so that they jump the exclusion zone
            if (x > screenWidthLessExclusionZone/2) x += sizeOfCenterExclusionZone;
            if (y > screenHeightLessExclusionZone/2) y += sizeOfCenterExclusionZone;
            movingWindow.setX(x);
            movingWindow.setY(y);
        }

        public void close()
        {
            //shut down the second window
            moveTimer.stop(); //kill the interval timer that is moving the window around
            movingWindow.close(); //close the window
        }

        public Stage getStage()
        {
            return movingWindow;
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
