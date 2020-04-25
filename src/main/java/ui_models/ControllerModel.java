package ui_models;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui_controllers.Controller;
import ui_controllers.InfoController;

import java.io.IOException;

public class ControllerModel {
    public Controller createNewWindow(String view, String title, Stage owner) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
        Parent root = loader.load();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        if(owner != null){
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
        }
        stage.show();
        Controller controller = (loader.getController());
        controller.init(stage);
        return controller;
    }

    public void createInfoWindow(String message, Stage owner) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/info_window.fxml"));
        Parent root = loader.load();
        stage.setTitle("Информация");
        stage.setScene(new Scene(root));
        if(owner != null){
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
        }
        InfoController controller = (loader.getController());
        controller.init(stage);
        controller.setMessage(message);
        stage.show();
    }
}
