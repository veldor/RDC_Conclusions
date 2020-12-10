package ui_controllers;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.handlers.FilesHandler;
import parser.FilesParser;
import sun.misc.Queue;

import java.io.File;
import java.io.IOException;

public class DbUpdateErrorController implements Controller{

    @FXML
    public
    Label errorTextLabel;
    private File mErrorFile;
    private Queue<String> mQueue;
    private MainController mParentController;
    private Stage mOwner;

    public void init(Stage owner) {
        mOwner = owner;
        owner.setOnCloseRequest(Event::consume);
    }

    @FXML
    public void openConclusion() throws IOException {
        FilesHandler.openFile(mErrorFile);
    }

    @FXML
    public void openFolder() throws IOException {
        FilesHandler.openContainingFolder(mErrorFile);
    }

    @FXML
    public void retryFileScan() {
        // продолжу процесс с проблемного места
        FilesParser filesParser = new FilesParser();
        filesParser.updateDatabase(mParentController);
        mOwner.close();
    }

    public void setActions(File f, MainController parentController) {
        mErrorFile = f;
        mParentController = parentController;
    }

    public void deleteProblemFile() {
        if(mErrorFile.delete()){
            retryFileScan();
        }
    }
}
