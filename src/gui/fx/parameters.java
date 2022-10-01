package gui.fx;

import handler.ReadPacket;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import lombok.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Scanner;

@Data
public class parameters {

    @FXML
    private GridPane param;
    @FXML
    private TableView validValues;
    @FXML
    private Label svIDLabel;
    @FXML
    private Label appIDLabel;
    @FXML
    private Label srcMACLabel;
    @FXML
    private Label desMACLabel;
    @FXML
    private Label confRevLabel;
    @FXML
    private Label smpSynchLabel;
    @FXML
    private Label smpCntLabel;

    private gui gui;

    public parameters(){
    }

    GridPane gridPane = new GridPane();

    public void setGui(gui gui) throws FileNotFoundException {
        this.gui = gui;
        File read = new File("src/gui/fx/parameters");
        Scanner scanner = new Scanner(read);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] nums = line.split(",");
            svIDLabel.setText(nums[1]);
            appIDLabel.setText(nums[2]);
            srcMACLabel.setText(nums[3]);
            desMACLabel.setText(nums[4]);
            confRevLabel.setText(nums[5]);
            smpSynchLabel.setText(nums[6]);
            smpCntLabel.setText(nums[0]);
            gridPane.getChildren().add(new Label());

            Connection connection;
            // Data and tabular representation
            ObservableList<ObservableList> data = FXCollections.observableArrayList();
            try {
                connection = ReadPacket.getConnection();
                // SQL to select all clients
                ResultSet rs = connection.createStatement().executeQuery("SELECT * from sampledvalues.validity");
                // Dynamic addition of a table column
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    // We use an unusual style to create a dynamic table
                    final int j = i;
                    TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                    col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>)
                            param -> new SimpleStringProperty(param.getValue().get(j).toString()));

                    validValues.getColumns().addAll(col);
                    System.out.println("Column [" + i + "] ");
                }
                // Data added to ObservableList
                while (rs.next()) {
                    // Repeat the line
                    ObservableList<String> row = FXCollections.observableArrayList();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        // Repeat column
                        row.add(rs.getString(i));
                    }
                    System.out.println("Row [1] added " + row);
                    data.add(row);

                }
                validValues.setItems(data);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error on Building Data");
            }
        }

    }

}


