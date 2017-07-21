package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.ArrayList;

public class Main extends Application {

    private ArrayList<Question> questions;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Build(primaryStage);
        primaryStage.setTitle(getClass().getName());
        //present the main stage
        primaryStage.show();
    }

    private void Build(Stage primaryStage)
    {
        // De fine a l a b e l
        Label label = new Label("Test Your Maths");

        // Place l a b e l i n a vbox
        VBox root = new VBox(label);
        Button check = new Button("Check");
        Button reset = new Button("Reset");

        //create some questions
        questions = new ArrayList<>();
        questions.add(new Question());
        questions.add(new Question());
        questions.add(new Question());

        //add questions to root VBox
        for(Question q : questions) // java 'foreach' syntax
        {
            q.addToVBox(root);
        }

        //insert the butons into their own HBox and imediatly insert that into root
        root.getChildren().add(new HBox(check,reset));

        // establish the actions to take when the user presses a button using lambda functions
        check.setOnAction(e -> {
            for(Question q : questions) // java 'foreach' syntax
            {
                q.evaluateAnswer();
            }
        });

        reset.setOnAction(e -> {
            for(Question q : questions) // java 'foreach' syntax
            {
                q.reset();
            }
        });


        // Set scene
        // insert root into scene
        Scene scene = new Scene(root, 500, 400);

        // Set stage
        // insert scene into main stage
        primaryStage.setScene(scene);


        // update some styles while the window is running, just cause we can
        // Set some style properties for  label and vbox
        label.setStyle("-fx-font-size: 50; -fx-text-fill: blue;");
        root.setStyle("-fx-alignment: center;");
    }


    public static void main(String[] args) {
        // not all compilers need this so you may fund examples on the web without it
        // but so far as I know there are none that will break if you include it so
        // it is safer and more portable if you have it in all you projects
        launch(args);
    }
}
