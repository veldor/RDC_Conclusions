package ui_controllers;

import db.Db;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Settings;
import models.handlers.FilesHandler;
import models.handlers.GrammarHandler;
import models.handlers.StringsHandler;
import models.handlers.WorkConclusionsHandler;
import parser.WordParser;
import selections.Conclusion;
import selections.ExecutionInfo;
import ui_models.ConclusionsControllerModel;
import utils.TooltippedTableCell;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConclusionsController implements Controller {

    @FXML
    public TableView<Conclusion> conclusionsInWorkTable;
    @FXML
    public TableColumn<Conclusion, String> executionNumberColumn;
    @FXML
    public TableColumn<Conclusion, String> personalsColumn;
    @FXML
    public TableColumn<Conclusion, String> birthDateColumn;
    @FXML
    public TableColumn<Conclusion, String> sexColumn;
    @FXML
    public TableColumn<Conclusion, String> executionAreaColumn;
    @FXML
    public TableColumn<Conclusion, String> contrastColumn;
    @FXML
    public TableColumn<Conclusion, String> executionDateColumn;
    @FXML
    public TableColumn<Conclusion, String> doctorColumn;

    public Stage mStage;
    private ConclusionsControllerModel mModel;

    // список описаний в работе
    private final ObservableList<Conclusion> conclusionsInWork = FXCollections.observableArrayList();

    @Override
    public void init(Stage owner) {
        conclusionsInWorkTable.setRowFactory(tv -> {
            TableRow<Conclusion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Conclusion rowData = row.getItem();
                    try {
                        FilesHandler.openFile(rowData.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            ContextMenu rowMenu = new ContextMenu();
            MenuItem openItem = new MenuItem("Внести в базу заключений");
            openItem.setOnAction(event -> {
                Conclusion rowData = row.getItem();
                try {
                    saveConclusion(rowData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            rowMenu.getItems().addAll(openItem);
            // todo разобраться, как работает этот код...
            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu) null));
            return row;
        });
        mStage = owner;
        mModel = new ConclusionsControllerModel();
        // устанавливаем тип и значение которое должно хранится в колонке
        executionNumberColumn.setCellValueFactory(new PropertyValueFactory<>("executionNumber"));
        executionNumberColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        personalsColumn.setCellValueFactory(new PropertyValueFactory<>("personals"));
        personalsColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthDateColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        sexColumn.setCellValueFactory(new PropertyValueFactory<>("sex"));
        sexColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        executionAreaColumn.setCellValueFactory(new PropertyValueFactory<>("executionArea"));
        executionAreaColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        executionDateColumn.setCellValueFactory(new PropertyValueFactory<>("executionDate"));
        executionDateColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        contrastColumn.setCellValueFactory(new PropertyValueFactory<>("contrast"));
        contrastColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("diagnostician"));
        doctorColumn.setCellFactory(TooltippedTableCell.forTableColumn());

        startWatching();

        conclusionsInWorkTable.setItems(conclusionsInWork);
    }

    private void saveConclusion(Conclusion conclusion) throws Exception {
        System.out.println(conclusion.getExecutionArea());
        if(conclusion.isFilled()){
            File oldFile = new File(conclusion.getPath());
            // теперь нужно понять, в какую папку заключение сохранять
            File pathToSave = FilesHandler.getPathToSave(conclusion);
            if(pathToSave != null){
                // сохраню заключение в данную папку и удалю из временной папки
                File newFile = new File(pathToSave, conclusion.getExecutionNumber() + "_" + conclusion.getPersonals() + StringsHandler.getExtension(conclusion.getFileName()));
                FilesHandler.copyFile(oldFile, newFile);
                // зарегистрирую новый файл в базе
                Db.getInstance().handleConclusion((new WordParser()).parse(newFile, false));
                if(!oldFile.delete()){
                    mModel.createInfoWindow("Не удалось удалить временный файл, удалите его вручную...", mStage);
                }
                //FilesHandler.copyFile(conclusion.getPath());
            }
            else{
                // не удалось понять, в какую папку сохранять заключение, попрошу выбрать папку для сохранения
                System.out.println("Не нашёл папку по умолчанию");
                //todo реализовать выбор папки и сохранить файл в выбранную
            }
        }
        else{
            System.out.println("Заключение заполнено не полностью...");
            // выведу окно с предложением верно заполнить заключение
            mModel.createInfoWindow("Заключение заполнено не полностью: " + conclusion.getSkipperParameter(), mStage);
        }
    }

    private void startWatching() {
        // добавлю сервис
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() {
                updateConclusionsList();
                // добавлю отслеживатель действий в папке
                try {
                    WatchService watchService = FileSystems.getDefault().newWatchService();
                    File conclusionFolder = Settings.getInstance().getTempDir();
                    Path path = Paths.get(conclusionFolder.toURI());

                    path.register(
                            watchService,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);

                    WatchKey key;
                    while ((key = watchService.take()) != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE) || event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                System.out.println("changing...");
                                // подожду немного, чтобы операция успела завершиться
                                Thread.sleep(100);
                                updateConclusionsList();
                            }
                        }
                        key.reset();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("finish");
                return null;
            }
        };
        new Thread(task).start();
    }

    public void requestFocus() {
        mStage.requestFocus();
    }

    // получу список заключений в работе
    private void updateConclusionsList() {
        System.out.println("start list update");
        ArrayList<Conclusion> conclusions = WorkConclusionsHandler.getConclusions();
        conclusionsInWork.clear();
        conclusionsInWork.addAll(conclusions);
        System.out.println("finish list update");
    }
}
