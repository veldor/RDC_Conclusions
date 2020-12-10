package ui_models;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sun.misc.Queue;
import ui_controllers.DbFillController;
import ui_controllers.ParseErrorController;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class DbFillModel extends ControllerModel {

    public DbFillModel(DbFillController controller) {
    }

    public void showErrorWindow(File f, Exception e, Stage mOwner, DbFillController parentController) {
        // создам окно с ошибкой
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/parse_error_window.fxml"));
            Parent root = loader.load();
            ParseErrorController controller = (loader.getController());
            controller.errorTextLabel.setText(String.format(Locale.ENGLISH, "Обнаружена ошибка при обработке файла: %s\n %s\nИсправьте или удалите файл и нажмите кнопку \"Повторить\"", f.getName(), e.getMessage()));
            stage.setTitle("Ошибка разбора заключения!");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mOwner);
            stage.show();
            controller.init(stage);
            controller.setActions(f, e, parentController);
        } catch (Exception er) {
            System.out.println("Ошибка при создании окна " + er.getMessage());
        }
    }
}
