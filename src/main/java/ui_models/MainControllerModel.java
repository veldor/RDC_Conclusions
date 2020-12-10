package ui_models;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui_controllers.ConclusionsController;
import ui_controllers.DbUpdateErrorController;
import ui_controllers.MainController;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MainControllerModel extends ControllerModel{
    private boolean mConclusionWindowCreated;
    private ConclusionsController mConclusionWindowController;

    public MainControllerModel(MainController controller){
    }

    public void openDbSettingsWindow(Stage stage) throws IOException {
        createNewWindow("/settings_window.fxml", "Настройки базы данных", stage);
    }

    public void openDbFillWindow(Stage stage) throws IOException {
        createNewWindow("/database_fill_window.fxml", "Настройки базы данных", stage);
    }

    public void activateConclusionsWindow() throws IOException {
        if(!mConclusionWindowCreated){
            mConclusionWindowController = (ConclusionsController) createNewWindow("/conclusions_window.fxml", "Заключения", null);
            mConclusionWindowCreated = true;
        }
        mConclusionWindowController.mStage.show();
        mConclusionWindowController.requestFocus();
    }

    public void showErrorWindow(File f, Exception e, Stage owner, MainController mainController) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/db_update_error_window.fxml"));
            Parent root = loader.load();
            DbUpdateErrorController controller = (loader.getController());
            controller.errorTextLabel.setText(String.format(Locale.ENGLISH, "Обнаружена ошибка при обработке файла: %s\n %s\nИсправьте или удалите файл и нажмите кнопку \"Повторить\"", f.getName(), e.getMessage()));
            stage.setTitle("Ошибка разбора заключения!");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.show();
            controller.init(stage);
            controller.setActions(f, mainController);
        } catch (Exception er) {
            System.out.println("Ошибка при создании окна " + er.getMessage());
        }
    }
}
