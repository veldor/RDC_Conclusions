package ui_models;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui_controllers.Controller;
import ui_controllers.DbSettingsController;
import ui_controllers.MainController;

import java.io.IOException;

public class MainControllerModel extends ControllerModel{
    private final MainController mController;

    public MainControllerModel(MainController controller) {
        mController = controller;
    }

    public void openDbSettingsWindow(Stage stage) throws IOException {
        createNewWindow("/database_settings_window.fxml", "Настройки базы данных", stage);
    }

    public void openDbFillWindow(Stage stage) throws IOException {
        createNewWindow("/database_fill_window.fxml", "Настройки базы данных", stage);
    }
}
