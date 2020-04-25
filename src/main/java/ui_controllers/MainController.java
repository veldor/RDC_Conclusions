package ui_controllers;

import db.Db;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import ui_models.MainControllerModel;

import java.io.IOException;
import java.sql.SQLException;

public class MainController implements Controller{

    private Stage mStage;
    private MainControllerModel mMyModel;


    @FXML
    public void openDatabaseSettings() throws IOException {
        // открою окно настроек базы данных
        mMyModel.openDbSettingsWindow(mStage);
    }
    @FXML
    public void openDatabaseFill() throws IOException {
        // открою окно заполнения базы данных
        mMyModel.openDbFillWindow(mStage);
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    public void init(Stage owner) {
        mMyModel = new MainControllerModel(this);
    }

    @FXML
    public void truncateDatabase() throws SQLException, IOException {
        Db.getInstance().clearData();
        mMyModel.createInfoWindow("База данных очищена!", mStage);
    }

    public void handleHotkeys(KeyEvent keyEvent) {
        
    }
}
