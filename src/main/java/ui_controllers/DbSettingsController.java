package ui_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.FilesUtils;
import models.Settings;
import models.handlers.GrammarHandler;

import java.io.IOException;

public class DbSettingsController implements Controller{
    private Stage mStage;

    @FXML
    private Label currentBaseFolder;

    @FXML
    public void changeDatabaseFolder() throws IOException {
        FilesUtils.changeDbFolder(mStage);
        changeDatabaseLocationLabel();
    }

    public void setStage(Stage settingsWindow) {
        mStage = settingsWindow;
    }

    public void init(Stage owner) {
        changeDatabaseLocationLabel();
    }

    private void changeDatabaseLocationLabel() {
        currentBaseFolder.setText(GrammarHandler.getDirValue(Settings.getInstance().getBaseDir()));
    }
}
