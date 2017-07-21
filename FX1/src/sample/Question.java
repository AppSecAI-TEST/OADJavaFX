package sample;

import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.lang.Math;

/**
 * Created by kroge on 20/07/2017.
 */
public class Question {
    private HBox box;
    private Label question;
    private Label result;
    private Boolean correct;
    private TextField reponce;
    private int value1;
    private int value2;

    public Question()
    {
        question = new Label();
        reponce = new TextField();
        result = new Label();
        reset();

        box = new HBox(question,reponce,result);
    }

    public boolean addToVBox (VBox vbox)
    {
        return (vbox.getChildren().add(box));
    }

    public boolean evaluateAnswer()
    {
        try {
            double userAnswer = Double.parseDouble(reponce.getText());
            double correctAnswer = value1 + value2;
            correct = (userAnswer == correctAnswer);
            if (correct) {
                result.setText("correct :)");
            } else {
                result.setText("incorrect :(");
            }
        }
        catch (Exception e)
        {
            correct = false;
            result.setText("Number format error");
        }
        return correct;
    }

    public void reset()
    {
        value1 = (int)Math.round(Math.random()*9)+1;
        value2 = (int)Math.round(Math.random()*9)+1;

        question.setText(value1 + " + " + value2 + " = ");
        reponce.setText(" ");
        result.setText(" ");
        correct = false;
    }

}
