package ui_controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import parser.FilesParser;
import sun.misc.Queue;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class ParseErrorController implements Controller{

    @FXML
    Label errorTextLabel;
    private File mErrorFile;
    private Queue<String> mQueue;
    private DbFillController mParentController;
    private Stage mOwner;

    public void init(Stage owner) {
        mOwner = owner;
        owner.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });
    }

    @FXML
    public void openConclusion() throws IOException {
        Desktop.getDesktop().open(mErrorFile);
    }

    @FXML
    public void openFolder() throws IOException {
        String command = String.format(Locale.ENGLISH, "explorer.exe /select, \"%s", mErrorFile.getParentFile().getCanonicalPath() + "\\" + mErrorFile.getName() + "\"");
        System.out.println(command);
        //Runtime.getRuntime().exec(command);
        Runtime.getRuntime().exec(new String[] {
                "explorer.exe",
                "/select,",
                "\"" + mErrorFile.getCanonicalPath() + "\""});

    }

    @FXML
    public void retryFileScan() throws InterruptedException {
        // добавлю проблемный файл первым в очередь
        Queue<String> newQueue = new Queue<String>();
        newQueue.enqueue(mErrorFile.getAbsolutePath());
        while (!mQueue.isEmpty()){
            newQueue.enqueue(mQueue.dequeue());
        }
        // продолжу процесс с проблемного места
        FilesParser filesParser = new FilesParser();
        filesParser.parse(newQueue, mParentController.parseFilesStatusView, mParentController);
        mOwner.close();
    }

    public void setActions(Queue<String> queue, File f, Exception e, DbFillController parentController) {
        mQueue = queue;
        mErrorFile = f;
        mParentController = parentController;
        // для начала, назначу текст сообщению об ошибке
        errorTextLabel.setText(String.format(Locale.ENGLISH, "Обнаружена ошибка при обработке файла: %s\n %s\nИсправьте или удалите файл и нажмите кнопку \"Повторить\"", f.getName(), e.getMessage()));
    }
}
