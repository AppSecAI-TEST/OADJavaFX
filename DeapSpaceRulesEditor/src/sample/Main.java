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
import java.util.Optional;
import java.sql.*;

public class Main extends Application {

    private ArrayList<Rule> rulesArray;
    private TableView<Rule> theTable;
    private ObservableList<Rule> tableData;
    private String fileName = "DarkSpaceRules.csv";
    String databaseURL = "jdbc:mysql://localhost:3306/darkspace";
    String username = "root";
    String password = "";
    private Connection connection = null;
    private Statement statement = null;




    @Override
    public void start(Stage primaryStage) throws Exception{

        // load the JDBC driver
        Class.forName("com.mysql.jdbc.Driver");

        rulesArray = new ArrayList<Rule>();
        tableData = FXCollections.observableArrayList(rulesArray);
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
        //Button saveChangesBtn = new Button("Save Changes to File");
        //saveChangesBtn.setOnAction(e->{
        //    saveRules(e);
        //});
        //buttonsContainer.getChildren().add(saveChangesBtn);

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

    public int updateOrInsert(Rule R)
    {
        try
        {
            // connect to the database
            connection = DriverManager.getConnection(databaseURL,username, password );

            // create a statement object
            statement = connection.createStatement();

            String updateRule = R.toSQLInsertOrUpdate();
            System.out.println(updateRule);
            int rs = statement.executeUpdate(updateRule);
            System.out.println("executeUpdate return: " + rs);

            connection.close();
            return rs;
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
        return -1;
    }

    public void update(ActionEvent e)
    {
        Rule selectedRule = theTable.getSelectionModel().getSelectedItem();
        rulesArray = new ArrayList<Rule>();
        tableData = FXCollections.observableArrayList(rulesArray);
        if(selectedRule == null) return;
        selectedRule.updateSelfFromTextFields();
        //force visual update on table view
        tableData.remove(selectedRule);
        tableData.add(selectedRule);

        updateOrInsert(selectedRule);
    }

    public void add(ActionEvent e)
    {
        Rule R = new Rule();
        R.updateSelfFromTextFields();
        tableData.add(R);
        rulesArray.add(R);

        updateOrInsert(R);

    }

    public void remove(ActionEvent e)
    {
        Rule selectedRule = theTable.getSelectionModel().getSelectedItem();
        if(selectedRule == null) return;
        tableData.remove(selectedRule);
        Rule.unsavedRulesChanges = true;

        try
        {
            // connect to the database
            connection = DriverManager.getConnection(databaseURL,username, password );

            // create a statement object
            statement = connection.createStatement();

            String deleteRule = selectedRule.toSQLDelete();
            System.out.println(deleteRule);
            int rs = statement.executeUpdate(deleteRule);
            System.out.println("executeUpdate return: " + rs);

            connection.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    public void saveRules(ActionEvent e)
    {
        try
        {
            // connect to the database
            connection = DriverManager.getConnection(databaseURL,username, password );

            // create a statement object
            statement = connection.createStatement();

            for (Rule R: tableData)
            {
                System.out.println(R.toSQLInsertOrUpdate());
                int rs = statement.executeUpdate(R.toSQLInsertOrUpdate());
                System.out.println("executeUpdate return: " + rs);
            }

            connection.close();
        }
        catch (Exception ex)
        {
            System.out.println(ex);

        }
    }

    public void loadRulesFile(String fileName)
    {
        theTable = Rule.buildTableView();

        try
        {
            // connect to the database
            connection = DriverManager.getConnection(databaseURL,username, password );

            // create a statement object
            statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("SELECT * FROM darkspacerules");

            theTable.setItems(tableData);

            while (rs.next())
            {
                //rs.getString(
                //rs.getDouble(
                HashMap<String,Object> row = new HashMap<>();
                row.put("ObjectName",rs.getString("ObjectName"));
                System.out.println(rs.getString("ObjectName"));
                row.put("foodP",new Double(rs.getDouble("foodP")));
                System.out.println(new Double(rs.getDouble("foodP")));
                row.put("waterP",new Double(rs.getDouble("waterP")));
                System.out.println(new Double(rs.getDouble("waterP")));
                row.put("housingP",new Double(rs.getDouble("housingP")));
                System.out.println(new Double(rs.getDouble("housingP")));
                row.put("elecP",new Double(rs.getDouble("elecP")));
                System.out.println(new Double(rs.getDouble("elecP")));
                row.put("metalP",new Double(rs.getDouble("metalP")));
                System.out.println(new Double(rs.getDouble("metalP")));
                row.put("metalBC",new Double(rs.getDouble("metalBC")));
                System.out.println(new Double(rs.getDouble("metalBC")));
                row.put("minutesBC",new Double(rs.getDouble("minutesBC")));
                System.out.println(new Double(rs.getDouble("minutesBC")));
                row.put("elecOC",new Double(rs.getDouble("elecOC")));
                System.out.println(new Double(rs.getDouble("elecOC")));
                row.put("popOC",new Double(rs.getDouble("popOC")));
                System.out.println(new Double(rs.getDouble("popOC")));
                row.put("healthBS",new Double(rs.getDouble("healthBS")));
                System.out.println(new Double(rs.getDouble("healthBS")));
                row.put("speedKmpsBS",new Double(rs.getDouble("speedKmpsBS")));
                System.out.println(new Double(rs.getDouble("speedKmpsBS")));
                row.put("attackBS",new Double(rs.getDouble("attackBS")));
                System.out.println(new Double(rs.getDouble("attackBS")));
                row.put("armorTypeBS",rs.getString("armorTypeBS"));
                System.out.println(rs.getString("armorTypeBS"));
                row.put("weaknessBS",rs.getString("weaknessBS"));
                System.out.println(rs.getString("weaknessBS"));
                row.put("strengthBS",rs.getString("strengthBS"));
                System.out.println(rs.getString("strengthBS"));
                row.put("modifireBS", new Double(rs.getString("modifireBS")));
                System.out.println(new Double(rs.getDouble("modifireBS")));
                row.put("ID", new Double(rs.getDouble("ID")));
                System.out.println(new Double(rs.getDouble("ID")));

                System.out.println(row);

                Rule R = new Rule(row);
                rulesArray.add(R);
                tableData.add(R);
            }

            connection.close();
        }
        catch (Exception e)
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
