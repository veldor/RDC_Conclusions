package ui_controllers;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import db.Db;
import db.SearchRequest;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import models.Settings;
import models.handlers.GrammarHandler;
import parser.FilesParser;
import selections.ExecutionInfo;
import ui_models.MainControllerModel;
import utils.TooltippedTableCell;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainController implements Controller {

    // ТАБЛИЦА =========================================================================================================

    @FXML
    public Button doSearchButton;

    @FXML
    public Button searchParamsResetButton;


    // список найденных обследований
    private final ObservableList<ExecutionInfo> foundedExecutions = FXCollections.observableArrayList();

    // список врачей
    private final ObservableList<String> doctorsList = FXCollections.observableArrayList();

    // список зон обследования
    private final ObservableList<String> executionAreas = FXCollections.observableArrayList();

    @FXML
    public TextField executionDateInput;

    @FXML
    public TextField executionDateFinishInput;

    @FXML
    public TextField textSearchInput;

    @FXML
    public ComboBox<String> doctorChoiceList;

    public VBox areasBoxContainer;
    public VBox mainContainer;

    @FXML
    public MenuItem refreshDatabaseMenuItem;

    /**
     * Поле внизу окна, отображающее статус совершаемой операции
     */
    @FXML
    public Label currentOperationStatus;

    @FXML
    private TextField patientPersonalsInput;

    // поле для ввода номера обследования
    @FXML
    private TextField executionIdInput;

    @FXML
    private TableView<ExecutionInfo> searchResultsTable;

    @FXML
    private TableColumn<ExecutionInfo, String> executionNumberColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> actionsColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> personalsColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> birthDateColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> executionAreaColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> executionDateColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> contrastColumn;

    @FXML
    private TableColumn<ExecutionInfo, String> changeDateColumn;

    private ComboBox<HideableItem<String>> mExecutionAreasList;

    // инициализируем форму данными
    @FXML
    private void initialize() throws SQLException {
        searchResultsTable.setRowFactory(tv -> {
            TableRow<ExecutionInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    ExecutionInfo rowData = row.getItem();
                    try {
                        rowData.openFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            ContextMenu rowMenu = new ContextMenu();
            MenuItem openItem = new MenuItem("Открыть");
            openItem.setOnAction(event -> {
                ExecutionInfo rowData = row.getItem();
                try {
                    rowData.openFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            MenuItem showItemInFolder = new MenuItem("Показать в папке");
            showItemInFolder.setOnAction(event -> {
                ExecutionInfo rowData = row.getItem();
                try {
                    rowData.openContainsFolder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            MenuItem useAsTemplate = new MenuItem("Использовать как шаблон");
            useAsTemplate.setOnAction(event -> {
                try {
                // проверю, если не назначена временная папка- оповещу об этом
                if(Settings.getInstance().getTempDir() == null){
                    mMyModel.createInfoWindow("Не назначена временная папка. Вы можете назначить её в меню \"Настройки\"", mStage);
                    return;
                }
                ExecutionInfo rowData = row.getItem();
                    rowData.useAsTemplate(rowData);
                } catch (IOException e) {
                    System.out.println("Не смог использовать файл как шаблон(");
                    e.printStackTrace();
                }
            });
            MenuItem searchByUser = new MenuItem("Найти все обследования пациента");
            searchByUser.setOnAction(event -> {
                System.out.println("search all patients executions...");
                ExecutionInfo rowData = row.getItem();
                try {
                    rowData.showAllByPatient(MainController.this);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
            rowMenu.getItems().addAll(openItem, showItemInFolder, useAsTemplate, searchByUser);

            // todo разобраться, как работает этот код...
            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(rowMenu)
                            .otherwise((ContextMenu) null));
            return row;
        });


        // устанавливаем тип и значение которое должно хранится в колонке
        executionNumberColumn.setCellValueFactory(new PropertyValueFactory<>("executionNumber"));
        executionNumberColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        personalsColumn.setCellValueFactory(new PropertyValueFactory<>("personals"));
        personalsColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthDateColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        executionAreaColumn.setCellValueFactory(new PropertyValueFactory<>("executionArea"));
        executionAreaColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        executionDateColumn.setCellValueFactory(new PropertyValueFactory<>("executionDate"));
        executionDateColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        contrastColumn.setCellValueFactory(new PropertyValueFactory<>("contrast"));
        contrastColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        changeDateColumn.setCellValueFactory(new PropertyValueFactory<>("changeDate"));
        changeDateColumn.setCellFactory(TooltippedTableCell.forTableColumn());
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));

        Callback<TableColumn<ExecutionInfo, String>, TableCell<ExecutionInfo, String>> cellFactory
                = //
                new Callback<TableColumn<ExecutionInfo, String>, TableCell<ExecutionInfo, String>>() {
                    @Override
                    public TableCell<ExecutionInfo, String> call(final TableColumn<ExecutionInfo, String> param) {
                        return new TableCell<ExecutionInfo, String>() {

                            final Hyperlink btn = new Hyperlink("Открыть файл");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        ExecutionInfo info = getTableView().getItems().get(getIndex());
                                        try {
                                            info.openFile();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    setGraphic(btn);
                                }
                                setText(null);
                            }
                        };
                    }
                };

        actionsColumn.setCellFactory(cellFactory);

        doctorsList.add("--");
        ArrayList<String> doctors = Db.getInstance().getDoctors();
        doctorsList.addAll(doctors);
        doctorChoiceList.setItems(doctorsList);

        executionAreas.add("--");
        ArrayList<String> areas = Db.getInstance().getExecutionAreas();
        executionAreas.addAll(areas);

        // ================================================ filtered list

        // todo разобраться, как работает и этот код тоже...
        mExecutionAreasList = createComboBoxWithAutoCompletionSupport(executionAreas);
        mExecutionAreasList.setPrefWidth(150);
        ComboBoxListViewSkin<HideableItem<String>> comboBoxListViewSkin = new ComboBoxListViewSkin<>(mExecutionAreasList);
        comboBoxListViewSkin.getPopupContent().addEventFilter(KeyEvent.ANY, (event) -> {
            if( event.getCode() == KeyCode.SPACE ) {
                event.consume();
            }
        });
        mExecutionAreasList.setSkin(comboBoxListViewSkin);
        areasBoxContainer.getChildren().add(mExecutionAreasList);

        //=================================================

        // заполняем таблицу данными
        searchResultsTable.setItems(foundedExecutions);
    }

    //==================================================================================================================


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
        mainContainer.prefWidthProperty().bind(owner.widthProperty());
        searchResultsTable.prefWidthProperty().bind(owner.widthProperty());
    }

    @FXML
    public void truncateDatabase() throws SQLException, IOException {
        Db.getInstance().clearData();
        mMyModel.createInfoWindow("База данных очищена!", mStage);
    }

    public void handleHotkeys(KeyEvent keyEvent) {
        System.out.println(keyEvent.getCode());
    }

    public void doSearch() throws SQLException, IOException {
        foundedExecutions.clear();
        String executionDate = executionDateInput.getText();
        // если присутствуют даты- сразу проверю их
        if(!executionDate.isEmpty()){
            try {
                executionDate = GrammarHandler.normalizeDateForDb(executionDate);
            }
            catch (Exception e){
                // неверно введена дата
                mMyModel.createInfoWindow("Неверная дата: " + executionDate, mStage);
                return;
            }
        }
        String executionDateFinish = executionDateFinishInput.getText();
        // если присутствуют даты- сразу проверю их
        if(!executionDateFinish.isEmpty()){
            try {
                executionDateFinish = GrammarHandler.normalizeDateForDb(executionDateFinish);
            }
            catch (Exception e){
                // неверно введена дата
                mMyModel.createInfoWindow("Неверная дата: " + executionDateFinish, mStage);
                return;
            }
        }
        String area = "";
        HideableItem<String> areaValue = mExecutionAreasList.getValue();
        if(areaValue != null){
            area = areaValue.toString();
        }
        // соберу данные из всех полей
        SearchRequest request = SearchRequest.newBuilder()
                .setExecutionId(executionIdInput.getText())
                .setPersonals(patientPersonalsInput.getText())
                .setExecutionDate(executionDate)
                .setExecutionDateFinish(executionDateFinish)
                .setTest(textSearchInput.getText())
                .setDoctor(doctorChoiceList.getValue())
                .setExecutionArea(area)
                .build();
        if(request.haveParameters()){
            // нормализирую дату
            executionDateInput.setText(GrammarHandler.normalizeDate(executionDateInput.getText()));
            executionDateFinishInput.setText(GrammarHandler.normalizeDate(executionDateFinishInput.getText()));

            ArrayList<ExecutionInfo> response = Db.getInstance().request(request);
            if(response.size() > 0){
                foundedExecutions.addAll(response);
            }
            else{
                mMyModel.createInfoWindow("Результаты не найдены", mStage);
            }
        }
        else{
            mMyModel.createInfoWindow("Выберите что-то для поиска", mStage);
        }
    }

    public void checkEnterForSearch(KeyEvent keyEvent) throws Exception{
        if(keyEvent.getCode().equals(KeyCode.ENTER)){
            doSearch();
        }
    }

    public void searchPatientExecutions(int patientId) throws SQLException {
        foundedExecutions.clear();
        SearchRequest request = SearchRequest.newBuilder().setPatientId(patientId).build();
        ArrayList<ExecutionInfo> response = Db.getInstance().request(request);
        foundedExecutions.addAll(response);
    }

    public void synchronizeFinishDate() {
        // синхронизирую дату с окном завершения периода
        executionDateFinishInput.setText(executionDateInput.getText());
    }

    public void dropSearchOptions() {
        executionDateFinishInput.setText("");
        executionDateInput.setText("");
        textSearchInput.setText("");
        executionIdInput.setText("");
        patientPersonalsInput.setText("");
        doctorChoiceList.setValue(null);
        doctorChoiceList.setValue(null);
    }

    public void openConclusionsHandler() throws IOException {
        // если окно ещё не открыто- открою, иначе- выведу на передний план
        mMyModel.activateConclusionsWindow();
    }

    public void refreshDatabase() throws IOException {
        // если сохранено значение последнего успешного обновления базы- обновлю её, иначе выдам окно
        // с оповещением о необходимости первичного заполнения базы
        if(Settings.getInstance().isBaseFilled()){
            // деактивирую пункт меню, чтобы не запускать процесс ещё раз
            refreshDatabaseMenuItem.setDisable(true);
            (new FilesParser()).updateDatabase(this);
        }
        else{
            mMyModel.createInfoWindow("Сначала нужно заполнить базу- База данных => Заполнить", mStage);
        }
    }

    public void successDbUpdate() {
        refreshDatabaseMenuItem.setDisable(false);
        try {
            mMyModel.createInfoWindow("База данных успешно актуализирована!", mStage);
            Settings.getInstance().baseUpdated(System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dbUpdateHasError(File f, Exception e) {
        // произошла ошибка при обработке файла, покажу окно с просьбой исправить ошибку и продолжить обработку файлов
        mMyModel.showErrorWindow(f, e, mStage, this);
    }


    public static class HideableItem<T>
    {
        private final ObjectProperty<T> object = new SimpleObjectProperty<>();
        private final BooleanProperty hidden = new SimpleBooleanProperty();

        private HideableItem(T object)
        {
            setObject(object);
        }

        private ObjectProperty<T> objectProperty(){return this.object;}
        private T getObject(){return this.objectProperty().get();}
        private void setObject(T object){this.objectProperty().set(object);}

        private BooleanProperty hiddenProperty(){return this.hidden;}
        private boolean isHidden(){return this.hiddenProperty().get();}
        private void setHidden(boolean hidden){this.hiddenProperty().set(hidden);}

        @Override
        public String toString()
        {
            return getObject() == null ? null : getObject().toString();
        }
    }

    private static <T> ComboBox<HideableItem<T>> createComboBoxWithAutoCompletionSupport(List<T> items)
    {
        ObservableList<HideableItem<T>> hideableHideableItems = FXCollections.observableArrayList(hideableItem -> new Observable[]{hideableItem.hiddenProperty()});

        items.forEach(item ->
        {
            HideableItem<T> hideableItem = new HideableItem<>(item);
            hideableHideableItems.add(hideableItem);
        });

        FilteredList<HideableItem<T>> filteredHideableItems = new FilteredList<>(hideableHideableItems, t -> !t.isHidden());

        ComboBox<HideableItem<T>> comboBox = new ComboBox<>();
        comboBox.setItems(filteredHideableItems);

        @SuppressWarnings("unchecked")
        HideableItem<T>[] selectedItem = (HideableItem<T>[]) new HideableItem[1];

        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, event ->
        {
            if(!comboBox.isShowing()) return;

            comboBox.setEditable(true);
            comboBox.getEditor().clear();
        });

        comboBox.showingProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue)
            {
                @SuppressWarnings("unchecked")
                ListView<HideableItem> lv = ((ComboBoxListViewSkin<HideableItem>) comboBox.getSkin()).getListView();

                Platform.runLater(() ->
                {
                    if(selectedItem[0] == null) // first use
                    {
                        double cellHeight = ((Control) lv.lookup(".list-cell")).getHeight();
                        lv.setFixedCellSize(cellHeight);
                    }
                });

                lv.scrollTo(comboBox.getValue());
            }
            else
            {
                HideableItem<T> value = comboBox.getValue();
                if(value != null) selectedItem[0] = value;

                comboBox.setEditable(false);

                Platform.runLater(() ->
                {
                    comboBox.getSelectionModel().select(selectedItem[0]);
                    comboBox.setValue(selectedItem[0]);
                });
            }
        });

        comboBox.setOnHidden(event -> hideableHideableItems.forEach(item -> item.setHidden(false)));

        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) ->
        {
            if(!comboBox.isShowing()) return;

            Platform.runLater(() ->
            {
                if(comboBox.getSelectionModel().getSelectedItem() == null)
                {
                    hideableHideableItems.forEach(item -> item.setHidden(!item.getObject().toString().toLowerCase().contains(newValue.toLowerCase())));
                }
                else
                {
                    boolean validText = false;

                    for(HideableItem hideableItem : hideableHideableItems)
                    {
                        if(hideableItem.getObject().toString().equals(newValue))
                        {
                            validText = true;
                            break;
                        }
                    }

                    if(!validText) comboBox.getSelectionModel().select(null);
                }
            });
        });
        return comboBox;
    }
}
