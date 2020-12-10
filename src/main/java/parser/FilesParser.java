package parser;

import db.Db;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import models.Settings;
import models.handlers.TimeHandler;
import selections.Conclusion;
import sun.misc.Queue;
import ui_controllers.DbFillController;
import ui_controllers.MainController;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FilesParser {
    public static int sFilesWithErrors = -1;
    public static int sTotalFlies;
    public static int sFilesHandled;
    public static long sStartTime;
    public static boolean sDatabaseEmpty;
    private File f;
    private Exception mCatchError;
    public static long start;
    Conclusion conclusion;
    private final Queue<String> mErrorQueue = new Queue<>();

    public static void singleParse(File f) throws Exception {
        Conclusion c = (new WordParser()).parse(f, false);
        if (c != null) {
            Db.getInstance().handleConclusion(c);
        }
    }

    public void updateDatabase(MainController mainController) {
        if (sFilesHandled < 0) {
            sFilesHandled = 0;
        }
        // запущу задачу на заполнение базы данных
        long lastUpdateTime = Settings.getInstance().getLastDbUpdateTime();
        if (lastUpdateTime > 0) {
            // запущу задачу
            Task<Void> task = new Task<Void>() {
                protected Void call() {
                    long oneConclusionHandleTime;
                        // получу очередь файлов для обработки
                        Queue<String> queue = (new QueueBuilder()).getUpdateQueue(lastUpdateTime);
                        updateMessage("Создана очередь обработки файлов");
                        if (queue != null && !queue.isEmpty()) {
                            while (!queue.isEmpty()) {
                                try {
                                updateMessage("Обработано файлов: " + sFilesHandled);
                                f = new File(queue.dequeue());
                                conclusion = (new WordParser()).parse(f, false);
                                if (conclusion != null) {
                                    Db.getInstance().handleConclusion(conclusion);
                                }
                            } catch (Exception e) {
                                System.out.println("have exception if thread " + e.getMessage());
                                mErrorQueue.enqueue(f.getAbsolutePath());
                                sFilesWithErrors++;
                            }
                                oneConclusionHandleTime = ((System.currentTimeMillis() - sStartTime) / (sFilesHandled + 1)) * (sTotalFlies - sFilesHandled);
                                this.updateMessage(String.format(Locale.ENGLISH, "Обработано %d из %d файлов \n Примерное время до завершения: %s\nФайлов с ошибками: %d", sFilesHandled, sTotalFlies, millisToTime(oneConclusionHandleTime), sFilesWithErrors));
                                this.updateProgress(sFilesHandled, sTotalFlies);
                                sFilesHandled++;
                        }
                    }
                    sFilesHandled = -1;
                    return null;
                }
            };

            // буду отображать ход задачи
            mainController.currentOperationStatus.textProperty().unbind();
            mainController.currentOperationStatus.textProperty().bind(task.messageProperty());

            task.setOnSucceeded(event -> {
                if (event != null && event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                    mainController.successDbUpdate();
                }
            });

            task.setOnFailed(event -> mainController.dbUpdateHasError(f, mCatchError));

            new Thread(task).start();
        }
    }

    public void parse(final Queue<String> queue, final DbFillController fillController) {
        if (sFilesHandled < 0) {
            sFilesHandled = 0;
        }
        if (sStartTime == 0) {
            sStartTime = System.currentTimeMillis();
        }

        if (sFilesWithErrors < 0) {
            sFilesWithErrors = 0;
        }
        // откреплю прикрепление текста к контроллеру, если оно есть
        fillController.parseFilesStatusView.textProperty().unbind();
        fillController.parseFilesStatusView.setText("Подготовка к обработке файлов");
        // погнали, проверю каждый из файлов и оповещу о результате
        Task<Void> task = new Task<Void>() {
            protected Void call() throws Exception {
                long oneConclusionHandleTime;
                // обработаю все файлы из очереди, после обработки каждого буду обновлять информацию об обработке
                try {
                    if(sDatabaseEmpty){
                        Db.getInstance().disableIndexes();
                    }
                    while (!queue.isEmpty()) {
                        try{
                            f = new File(queue.dequeue());
                            if (f.isFile() && (sDatabaseEmpty || !Db.getInstance().checkFile(f))) {
                                //TimeHandler.notifyTime("Начата обработка");
                                // отправлю файл на обработку
                                conclusion = (new WordParser()).parse(f, false);
                                //TimeHandler.notifyTime("Обработано заключение");
                                if (conclusion != null) {
                                    Db.getInstance().handleConclusion(conclusion);
                                }
                                //TimeHandler.notifyTime("Заключение добавлено в базу данных");
                            }
                            else{
                                TimeHandler.notifyTime("Пропущен файл, имеющийся в БД");
                            }
                        }
                        catch (Exception e) {
                            System.out.println("have exception if thread " + e.getMessage());
                            mErrorQueue.enqueue(f.getAbsolutePath());
                            sFilesWithErrors++;
                        }
                        sFilesHandled++;
                        // посчитаю время до завершения. Для этого возьму разницу во времени с начала обследования
                        // и поделю её на количество обработанных заключений. Это будет время, потраченное
                        // на обработку одного заключения. Верну это время, умноженное на
                        // количество оставшихся заключений
                        oneConclusionHandleTime = ((System.currentTimeMillis() - sStartTime) / (sFilesHandled + 1)) * (sTotalFlies - sFilesHandled);
                        this.updateMessage(String.format(Locale.ENGLISH, "Обработано %d из %d файлов \n Примерное время до завершения: %s\nФайлов с ошибками: %d", sFilesHandled, sTotalFlies, millisToTime(oneConclusionHandleTime), sFilesWithErrors));
                        this.updateProgress(sFilesHandled, sTotalFlies);
                        //TimeHandler.notifyTime("Обработка закончена");
                    }
                } catch (Exception e) {
                    mCatchError = e;
                    throw e;
                }
                System.out.println("spend " + (System.currentTimeMillis() - start));
                if(sDatabaseEmpty){
                    System.out.println("start enable indexes");
                    Db.getInstance().enableIndexes();
                    System.out.println("finish enable indexes");
                }
                // покажу лог парсинга
                //FilesHandler.showLog();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (event != null && event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                sFilesHandled = -1;
                sStartTime = 0;
                sFilesWithErrors = -1;
                try {
                    fillController.filesParsed(mErrorQueue);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        task.setOnFailed(event -> {
            // возникла проблема в обработке файла. Нужно пропустить проблемный файл и обработать его, когда закончатся
            // нормальные файлы. Проплюсую счётчик файлов с ошибками и помещу файл в конец очереди
            // если число файлов с ошибками равно числу оставшихся необработанных файлов- вызову окно
            // с предложением исправить ошибку, иначе- просто перезапущу сканирование
            if (sTotalFlies - sFilesHandled > sFilesWithErrors) {
                System.out.println("error...");
                // скопирую файл в папку с неразобранными
                /*try {
                    FilesHandler.copyFile(f, FilesHandler.getUnhandledPath(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                queue.enqueue(f.getAbsolutePath());
                ++sFilesWithErrors;
                FilesParser filesParser = new FilesParser();
                filesParser.parse(queue, fillController);
            } else {
                System.out.println("spend " + (System.currentTimeMillis() - start));
                // добавлю проблемный файл в конец очереди
                queue.enqueue(f.getAbsolutePath());
                // при получении ошибки вызову сообщение с призывом её исправить
                try {
                    fillController.haveParseError(queue, f, mCatchError, fillController);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Ошибка при создании окна с ошибкой парсинга");
                }
            }

        });

        fillController.parseFilesStatusView.textProperty().bind(task.messageProperty());
        fillController.parseProgress.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    private String millisToTime(long millis) {
        return String.format("%02d ч. %02d м. %02d с.",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }
}
