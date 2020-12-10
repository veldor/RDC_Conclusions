package ui_controllers;

import db.Db;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import models.Settings;
import parser.FilesMultiThreadParser;
import parser.FilesParser;
import parser.QueueBuilder;
import sun.misc.Queue;
import ui_models.DbFillModel;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class DbFillController implements Controller {

    @FXML
    public ProgressBar parseProgress;

    @FXML
    public Button handleErrorFilesButton;

    @FXML
    Button fillQueueButton;

    @FXML
    Button parseFilesButton;

    @FXML
    Label fillQueueStatusView;

    @FXML
    public
    Label parseFilesStatusView;

    private Queue<String> mQueue;
    private DbFillModel mMyModel;
    private Stage mOwner;
    private Queue<String> mErrorQueue;
    private long mParsingStart;

    @FXML
    public void fillFilesQueue() {
        // запущу сканирование файлов в папке заключений
        // заблокирую кнопку
        fillQueueButton.setDisable(true);
        // запущу сканирование файлов
        QueueBuilder queueBuilder = new QueueBuilder();
        // заполню очередь
        queueBuilder.fillQueue(fillQueueStatusView, this, System.currentTimeMillis());
    }

    @FXML
    public void parseFiles() throws SQLException {
        System.out.println("start parse");
        FilesMultiThreadParser.start = System.currentTimeMillis();
        mParsingStart = System.currentTimeMillis();
        // если база пуста- не буду проверять наличие в ней файлов а буду сразу записывать найденные данные
        FilesMultiThreadParser.sDatabaseEmpty = Db.getInstance().isDatabaseEmpty();
        FilesMultiThreadParser filesParser = new FilesMultiThreadParser();
        filesParser.parse(mQueue, this);
        parseFilesButton.setDisable(true);
    }

    public void init(Stage owner) {
        mMyModel = new DbFillModel(this);
        mOwner = owner;
    }

    /**
     * Этот метод вызовет создатель очереди файлов
     *
     * @param queue <p>Очередь заключений</p>
     */
    public void queueCreated(Queue<String> queue){
        mQueue = queue;
        parseFilesButton.setDisable(false);
        parseFilesStatusView.setVisible(true);
        parseFilesStatusView.setText("Готов к обработке файлов.");
    }

    public void filesParsed(Queue<String> errorQueue) throws IOException {
        mErrorQueue = errorQueue;
        // ура, файлы обработаны.
        fillQueueButton.setDisable(false);
        parseFilesStatusView.textProperty().unbind();
        Settings.getInstance().baseUpdated(System.currentTimeMillis());
        // проверю, если нет нераспознанных файлов- оповещу об этом
        if(errorQueue.isEmpty()){
            parseFilesStatusView.setText("Все файлы успешно обработаны");
            mMyModel.createInfoWindow("База актуализирована", mOwner);
        }
        else{
            // активирую кнопку обработки исключений
            handleErrorFilesButton.setDisable(false);
            mMyModel.createInfoWindow("Обработка заняла " + (System.currentTimeMillis() - mParsingStart) + " миллисекунд", mOwner);
            mMyModel.createInfoWindow("При обработке найдены ошибки. Нажмите на кнопку \"Обработать файлы с ошибками, чтобы исправить данные\"", mOwner);

        }
    }

    public void haveParseError(Queue<String> queue, File f, Exception e, DbFillController controller) throws IOException {
        // вызову окно с ошибкой
        mMyModel.showErrorWindow(f, e, mOwner, controller);
    }

    public void parseErrorFiles() throws InterruptedException {
        // получу первый файл из очереди и выведу окно с ошибкой файла
        if(!mErrorQueue.isEmpty()){
            File f = new File(mErrorQueue.dequeue());
            parseErrorFile(f);
        }
    }

    public void parseErrorFile(File f) {
        try{
            FilesParser.singleParse(f);
            if(mErrorQueue != null){
                parseErrorFile(new File(mErrorQueue.dequeue()));
            }
        }
        catch (Exception e){
            mMyModel.showErrorWindow(f, e, mOwner, this);
        }
    }

    public void showInfo(long l) {
        System.out.println("showing window");
        try {
            mMyModel.createInfoWindow("Прошло всего " + l + " миллисекунд!", mOwner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
