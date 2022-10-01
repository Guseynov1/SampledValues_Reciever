package gui.fx;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class gui extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Sampled Values");
        showGUI();
    }

    public void showGUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gui.fxml"));
            Parent gui = loader.load();
            Scene scene = new Scene(gui);
            stage.setScene(scene);
            stage.show();
            parameters parameters = loader.getController();
            parameters.setGui(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
