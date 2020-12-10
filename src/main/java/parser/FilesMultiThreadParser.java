package parser;

import db.Db;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import models.handlers.TimeHandler;
import selections.Conclusion;
import sun.misc.Queue;
import ui_controllers.DbFillController;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FilesMultiThreadParser {
    public static int sFilesWithErrors = -1;
    public static int sTotalFlies;
    public static int sFilesHandled;
    public static long sStartTime;
    public static boolean sDatabaseEmpty;
    public static long start;
    Conclusion conclusion;

    private final Queue<String> mErrorQueue = new Queue<>();
    private Queue<String> mQueue;

    public void parse(final Queue<String> queue, final DbFillController fillController) {
        System.out.println("i am in parser");
        mQueue = queue;
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
                System.out.println("parser main task started");
                long oneConclusionHandleTime;
                // тут запущу несколько потоков, которые будут обрабатывать заключения и буду отслеживать выполнение
                // пока очередь заключений не опустеет
                // обработаю все файлы из очереди, после обработки каждого буду обновлять информацию об обработке
                if (sDatabaseEmpty) {
                    System.out.println("database empty, disable indexes");
                    Db.getInstance().disableIndexes();
                }
                Db.getInstance().disableForeignChecks();
                System.out.println("prepare creating threads");
                // для каждого ядра процессора запущу по потоку
                int cores = Runtime.getRuntime().availableProcessors();
                System.out.println("found " + cores + " cores");
                while (cores > 0) {
                    System.out.println("start new thread");
                    new Thread(new ThreadParser()).start();
                    cores--;
                }
                while (!mQueue.isEmpty()) {
                    //noinspection BusyWait
                    Thread.sleep(50);
                    oneConclusionHandleTime = ((System.currentTimeMillis() - sStartTime) / (sFilesHandled + 1)) * (sTotalFlies - sFilesHandled);
                    // посчитаю % готовности
                    double percentDone =((double)sFilesHandled * 100 / (double)sTotalFlies);
                    this.updateMessage(String.format(Locale.ENGLISH, "Обработано %d из %d файлов \n (%.3f%%) Примерное время до завершения: %s\nФайлов с ошибками: %d", sFilesHandled, sTotalFlies, percentDone, millisToTime(oneConclusionHandleTime), sFilesWithErrors));
                    this.updateProgress(sFilesHandled, sTotalFlies);
                }
                Thread.sleep(1000);
                System.out.println("spend " + (System.currentTimeMillis() - start));
                if(sDatabaseEmpty){
                    this.updateMessage("Файлы обработаны, проверяю результат, это займёт время...");
                    try {
                        Db.getInstance().enableForeignChecks();
                        Db.getInstance().enableIndexes();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                this.updateMessage(String.format(Locale.ENGLISH, "Обработано %d файлов \n nФайлов с ошибками: %d", sTotalFlies, sFilesWithErrors));
                // покажу лог парсинга
                //FilesHandler.showLog();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            System.out.println("all handled");
            if (event != null && event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                sFilesHandled = -1;
                sStartTime = 0;
                try {
                    fillController.filesParsed(mErrorQueue);
                } catch (IOException e) {
                    e.printStackTrace();
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

    private class ThreadParser implements Runnable {

        private File fileInProgress;

        @Override
        public void run() {
            // обработаю файл
            while (!mQueue.isEmpty()) {
                try {
                    synchronized (mErrorQueue) {
                        fileInProgress = new File(mQueue.dequeue());
                    }
                    if (fileInProgress.isFile() && (sDatabaseEmpty || !Db.getInstance().checkFile(fileInProgress))) {
                        // отправлю файл на обработку
                        conclusion = (new WordParser()).parse(fileInProgress, false);
                        // TimeHandler.notifyTime("Обработано заключение");
                        if (conclusion != null) {
                            Db.getInstance().handleConclusion(conclusion);
                        }
                        // TimeHandler.notifyTime("Заключение добавлено в базу данных");
                    } else {
                        TimeHandler.notifyTime("Пропущен файл, имеющийся в БД");
                    }
                } catch (Exception e) {
                    System.out.println("have exception if thread " + Thread.currentThread().getName() + " " + e.getMessage() + " on file " + fileInProgress.getName());
                    mErrorQueue.enqueue(fileInProgress.getAbsolutePath());
                    sFilesWithErrors++;
                }
                sFilesHandled++;
            }
        }
    }
}
