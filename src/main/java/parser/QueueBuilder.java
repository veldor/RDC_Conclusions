package parser;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Label;
import models.Settings;
import models.handlers.FilesHandler;
import sun.misc.Queue;
import ui_controllers.DbFillController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class QueueBuilder {

    private Queue<String> mQueue;
    private int mQueueAddCounter = 1;


    ArrayList<File> mFiles;
    File[] mContent;

    public void fillQueue(Label label, final DbFillController controller, final long start) {

        final File dir = Settings.getInstance().getBaseDir();
        // проверю, добавлена ли папка с заключениями
        if (dir == null) {
            return;
        }
        Task<Void> task = new Task<Void>() {

            protected Void call(){
                // обработаю все файлы из очереди, после обработки каждого буду обновлять информацию об обработке
                // получу папку с заключениями
                File dir = Settings.getInstance().getBaseDir();
                mQueue = new Queue<>();
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
                    mFiles = new ArrayList<>(Arrays.asList(mContent));
                    if (mFiles.size() > 0) {
                        for (File f :
                                mFiles) {
                            // если элемент- папка, придётся рекурсивно читать её. Если файл- проверю, что это файл,
                            // доступный для обработки
                            if (f.isDirectory()) {
                                recursiveAddToQueue(f);
                            } else if (f.isFile()) {
                                if(f.getName().startsWith("~$")){
                                    continue;
                                }
                                String extension = FilesHandler.getExtension(f.getName());
                                // если файл подходит и ещё не в базе- добавлю его в список для конвертации
                                if (FilesHandler.accept(extension)) {
                                    // если файл в doc- создам его копию в docx и удалю оригинал
                                    if(extension.equals("doc")){
                                        f = FilesHandler.recreate(f);
                                    }
                                    ++mQueueAddCounter;
                                    mQueue.enqueue(f.getAbsolutePath());
                                    // оповещу о прогрессе
                                        this.updateMessage("Добавлено файлов: " + mQueueAddCounter);
                                }
                            }
                        }
                        this.updateMessage("Добавлено файлов: " + mQueueAddCounter);
                    }
                }
            }
        };
        task.setOnSucceeded(event -> {
            if (event != null && event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                controller.queueCreated(mQueue);
                controller.showInfo(System.currentTimeMillis() - start);
                FilesParser.sTotalFlies = mQueueAddCounter;
                FilesMultiThreadParser.sTotalFlies = mQueueAddCounter;
            }
        });
        task.setOnFailed(event -> System.out.println("failed( " + event.toString()));
        label.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }

    public Queue<String> getUpdateQueue(long lastUpdateTime) {
        mQueue = new Queue<>();
        File dir = Settings.getInstance().getBaseDir();
        // получу список файлов, дата обновления которых больше, чем время последнего обновления
        recursiveAddToQueue(dir, lastUpdateTime);
        return mQueue;
    }


    private void recursiveAddToQueue(File entity, long updateTime){
        // проверю содержимое папки
        mContent = entity.listFiles();
        if (mContent != null) {
            mFiles = new ArrayList<>(Arrays.asList(mContent));
            if (mFiles.size() > 0) {
                for (File f :
                        mFiles) {
                    // если элемент- папка, придётся рекурсивно читать её. Если файл- проверю, что это файл,
                    // доступный для обработки
                    if (f.isDirectory()) {
                        recursiveAddToQueue(f, updateTime);
                    } else if (f.isFile()) {
                        if(f.getName().startsWith("~$")){
                            continue;
                        }
                        String extension = FilesHandler.getExtension(f.getName());
                        // если файл подходит и ещё не в базе- добавлю его в список для конвертации
                        if (FilesHandler.accept(extension) && f.lastModified() > updateTime) {
                            mQueue.enqueue(f.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
}
