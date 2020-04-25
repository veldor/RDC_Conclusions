package ui_models;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.stage.Stage;
import sun.misc.Queue;
import ui_controllers.Controller;
import ui_controllers.DbFillController;
import ui_controllers.ParseErrorController;

import java.io.File;
import java.io.IOException;

public class DbFillModel extends ControllerModel{

    private final DbFillController mController;

    public DbFillModel(DbFillController controller) {
        mController = controller;
    }

    public void showErrorWindow(Queue<String> queue, File f, Exception e, Stage mOwner, DbFillController parentController) throws IOException {
        System.out.println("Начинаю создание окна");
        // создам окно с ошибкой
        try{
            ParseErrorController controller = (ParseErrorController) createNewWindow("/parse_error_window.fxml", "Ошибка разбора заключения!", mOwner);
            controller.setActions(queue, f, e, parentController);
        }
        catch (Exception er){
            System.out.println("Ошибка при создании окна " + er.getMessage());
        }
        System.out.println("Модель создала новое окно");
    }
}
