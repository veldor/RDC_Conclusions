package ui_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import parser.FilesParser;
import parser.QueueBuilder;
import sun.misc.Queue;
import ui_models.DbFillModel;

import java.io.File;
import java.io.IOException;

public class DbFillController implements Controller{

    @FXML
    Button fillQueueButton;

    @FXML
    Button parseFilesButton;

    @FXML
    Label fillQueueStatusView;

    @FXML
    Label parseFilesStatusView;

    private Queue<String> mQueue;
    private DbFillModel mMyModel;
    private Stage mOwner;

    @FXML
    public void fillFilesQueue(){
        // запущу сканирование файлов в папке заключений
        // заблокирую кнопку
        fillQueueButton.setDisable(true);
        // запущу сканирование файлов
        QueueBuilder queueBuilder = new QueueBuilder();
        // заполню очередь
        queueBuilder.fillQueue(fillQueueStatusView, this);
    }

    @FXML
    public void parseFiles() throws IOException {
        FilesParser filesParser = new FilesParser();
        filesParser.parse(mQueue, parseFilesStatusView, this);
        parseFilesButton.setDisable(true);
    }

    public void init(Stage owner){
        mMyModel = new DbFillModel(this);
        mOwner = owner;
    }

    public void update(String status) {
        // сюда будут приходить события от обработчика очереди файлов
        fillQueueStatusView.setText(status);
    }

    /**
     * Этот метод вызовет создатель очереди файлов
     * @param queue <p>Очередь заключений</p>
     */
    public void queueCreated(Queue<String> queue) throws IOException {
        mQueue = queue;
        parseFilesButton.setDisable(false);
        parseFilesStatusView.setVisible(true);
        parseFilesStatusView.setText("Готов к обработке файлов.");
        mMyModel.createInfoWindow("Файлы подготовлены для обработки. Нажмите на кнопку 'Обработать файлы' для продолжения", mOwner);
    }

    public void filesParsed() throws IOException {
        // ура, файлы обработаны.
        fillQueueButton.setDisable(false);
        parseFilesStatusView.textProperty().unbind();
        parseFilesStatusView.setText("Все файлы успешно обработаны");
        mMyModel.createInfoWindow("База актуализирована", mOwner);
    }

    public void haveParseError(Queue<String> queue, File f, Exception e, DbFillController controller) throws IOException {
        System.out.println("Контроллер получил данные об ошибке");
        // вызову окно с ошибкой
        mMyModel.showErrorWindow(queue, f, e, mOwner, controller);
    }
}
