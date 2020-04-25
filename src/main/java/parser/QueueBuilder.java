package parser;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import models.Settings;
import models.handlers.FilesHandler;
import sun.misc.Queue;
import ui_controllers.DbFillController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class QueueBuilder {

    private Queue<String> mQueue;

    public void fillQueue(Label label, final DbFillController controller) {

        final File dir = Settings.getInstance().getBaseDir();
        // проверю, добавлена ли папка с заключениями
        if (dir == null) {
            return;
        }
        Task<Void> task = new Task<Void>() {

            ArrayList<File> mFiles;
            int mQueueAddCounter = 1;
            File[] mContent;

            protected Void call(){
                // обработаю все файлы из очереди, после обработки каждого буду обновлять информацию об обработке
                // получу папку с заключениями
                File dir = Settings.getInstance().getBaseDir();
                mQueue = new Queue<String>();
                long start = System.currentTimeMillis();
                    try{
                        // очищу файл очереди
                        recursiveAddToQueue(dir);
                        System.out.println("spend " + (System.currentTimeMillis() - start));
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                return null;
            }

            private void recursiveAddToQueue(File entity){
                // проверю содержимое папки
                mContent = entity.listFiles();
                if (mContent != null) {
                    mFiles = new ArrayList<File>(Arrays.asList(mContent));
                    if (mFiles.size() > 0) {
                        for (File f :
                                mFiles) {
                            // если элемент- папка, придётся рекурсивно читать её. Если файл- проверю, что это файл,
                            // доступный для обработки
                            if (f.isDirectory()) {
                                recursiveAddToQueue(f);
                            } else if (f.isFile()) {
                                String extension = FilesHandler.getExtension(f.getName());
                                // если файл подходит и ещё не в базе- добавлю его в список для конвертации
                                if (FilesHandler.accept(extension)) {
                                    ++mQueueAddCounter;
                                    mQueue.enqueue(f.getAbsolutePath());
                                    // оповещу о прогрессе
                                    if(mQueueAddCounter % 100 == 0){
                                        this.updateMessage("Добавлено файлов: " + mQueueAddCounter);
                                    }
                                }
                            }
                        }
                        this.updateMessage("Добавлено файлов: " + mQueueAddCounter);
                    }
                }
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent event) {
                if (event != null && event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                    try {
                        controller.queueCreated(mQueue);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent event) {
                System.out.println("failed( " + event.toString());
            }
        });
        label.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }
}
