package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Optional;

public class Main extends Application {

    private ArrayList<Rule> rulesArray;
    private TableView<Rule> theTable;
    private ObservableList<Rule> tableData;
    private String fileName = "DarkSpaceRules.csv";



    @Override
    public void start(Stage primaryStage) throws Exception{
        rulesArray = new ArrayList<Rule>();
        loadRulesFile(fileName);
        build(primaryStage);
        primaryStage.show();
    }

    public void build(Stage primaryStage)
    {
        //text fields for data entree
        //buttons
        // add, update, saveToFile
        //table for displaying current rules
        HBox textFields = Rule.buildTextFields();
        VBox root = new VBox();
        HBox buttonsContainer = new HBox();

        //load
        Button loadBtn = new Button("Load Selected Rule");
        loadBtn.setTooltip( new Tooltip("use object_type to fill the remaining Text Fields with that objects current rules"));
        ///*
        loadBtn.setOnAction(e->load(e));//*/
        buttonsContainer.getChildren().add(loadBtn);
        //update
        Button updateBtn = new Button("Update Selected Rule");
        updateBtn.setOnAction(e->{
            update(e);
        });
        buttonsContainer.getChildren().add(updateBtn);
        //add
        Button addBtn = new Button("Add New Rule");
        addBtn.setOnAction(e->{
            add(e);
        });
        buttonsContainer.getChildren().add(addBtn);
        //remove
        Button removeBtn = new Button("Remove Selected Rule");
        removeBtn.setOnAction(e->{
            remove(e);
        });
        buttonsContainer.getChildren().add(removeBtn);
        //saveChanges (taps into property listener)
        Button saveChangesBtn = new Button("Save Changes to File");
        saveChangesBtn.setOnAction(e->{
            saveRules(e);
        });
        buttonsContainer.getChildren().add(saveChangesBtn);



        root.getChildren().addAll(buttonsContainer,textFields,theTable);
        primaryStage.setScene(new Scene(root));

    }

    private void load(ActionEvent e)
    {
        //use object_type to fill the remaining Text Fields with that objects current rules
        if (Rule.unsavedIndividualRuleChanges)
        {
            Alert A = new Alert(Alert.AlertType.CONFIRMATION,"un-updated changes will be lost\nare you sure you want to continue");
            Optional<ButtonType> userResult = A.showAndWait();
            if (!userResult.isPresent()) return;
            if (userResult.get() == ButtonType.CANCEL) return;
        }

        Rule selectedRule = theTable.getSelectionModel().getSelectedItem();
        if(selectedRule == null) return;
        selectedRule.loadSelfIntoTextFields();
    }

    public void update(ActionEvent e)
    {
        Rule selectedRule = theTable.getSelectionModel().getSelectedItem();
        if(selectedRule == null) return;
        selectedRule.updateSelfFromTextFields();
        //force visual update on table view
        tableData.remove(selectedRule);
        tableData.add(selectedRule);
    }

    public void add(ActionEvent e)
    {
        Rule R = new Rule();
        R.updateSelfFromTextFields();
        tableData.add(R);
        rulesArray.add(R);
    }

    public void remove(ActionEvent e)
    {
        Rule selectedRule = theTable.getSelectionModel().getSelectedItem();
        if(selectedRule == null) return;
        tableData.remove(selectedRule);
        Rule.unsavedRulesChanges = true;
    }

    public void saveRules(ActionEvent e)
    {
        try
        {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter(fileName,false));
            bWriter.write(Rule.headersToCSV());
            for (Rule R: tableData)
            {
                bWriter.write( "\n" + R.toCSV());
            }
            bWriter.flush();
            bWriter.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    public void loadRulesFile(String fileName)
    {
        //check if file exists
        File fileCheck = new File(fileName);
        tableData = FXCollections.observableArrayList();
        try {
            if (!fileCheck.exists()) {
                BufferedWriter newRules = new BufferedWriter(new FileWriter(fileName,false));
                String HeadingsLine = Rule.defaultColumnHeadings[0];
                for (int i = 1; i < Rule.defaultColumnHeadings.length; i++)
                {
                    HeadingsLine += "," + Rule.defaultColumnHeadings[i];
                }
                newRules.write(HeadingsLine + "\n");
                newRules.flush();
                newRules.close();
                Rule.loadHeadings(HeadingsLine);
                theTable = Rule.buildTableView();
            }
            else
            {
                BufferedReader rules = new BufferedReader(new FileReader(fileName));
                String HeadingsLine = rules.readLine();
                Rule.loadHeadings(HeadingsLine);
                theTable = Rule.buildTableView();
                theTable.setItems(tableData);
                String line = rules.readLine();
                while (line != null){
                    Rule tempRule = new Rule(line);
                    rulesArray.add(tempRule);
                    tableData.add(tempRule);
                    line = rules.readLine();
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        //if not create file with 1st line column headings
        //else load file
    }

    public static void main(String[] args) {
        launch(args);
    }
}
