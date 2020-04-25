package parser;

import db.Db;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import models.handlers.FilesHandler;
import selections.Conclusion;
import sun.misc.Queue;
import ui_controllers.DbFillController;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class FilesParser {
    private File f;
    private Exception mCatchError;

    public void parse(final Queue<String> queue, Label label, final DbFillController fillController){
        // откреплю прикрепление текста к контроллеру, если оно есть
        label.textProperty().unbind();
        label.setText("Подготовка к обработке файлов");
        // погнали, проверю каждый из файлов и оповещу о результате
        Task<Void> task = new Task<Void>() {
            protected Void call() throws Exception {
                long start = System.currentTimeMillis();
                // обработаю все файлы из очереди, после обработки каждого буду обновлять информацию об обработке
                Conclusion conclusion;
                int currentParseCounter = 0;
                try{
                    while (!queue.isEmpty()) {
                        f = new File(queue.dequeue());
                        if (f.isFile() && !Db.getInstance().checkFile(f)) {
                            // отправлю файл на обработку
                            conclusion = WordParser.getInstance().parse(f);
                            if(conclusion != null){
                                Db.getInstance().handleConclusion(conclusion);
                            }
                        }
                        else{
                            System.out.println("skip file existent in db");
                        }
                        currentParseCounter++;
                        this.updateMessage(String.format(Locale.ENGLISH, "Обработано %d  файлов", currentParseCounter));
                    }
                }
                catch (Exception e){
                    mCatchError = e;
                    System.out.println("catch error " + e.getMessage());
                    throw e;
                }
                System.out.println("spend " + (System.currentTimeMillis() - start));
                // покажу лог парсинга
                FilesHandler.showLog();
                return null;
            }
        };
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent event) {
                if (event != null && event.getEventType() == WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                    try {
                        fillController.filesParsed();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent event) {
                System.out.println("failed( " + event.toString());
                // при получении ошибки вызову сообщение с призывом её исправить
                try {
                    fillController.haveParseError(queue, f, mCatchError, fillController);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Ошибка при создании окна с ошибкой парсинга");
                }
            }
        });
        label.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }
}
