package models;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;


public class FilesUtils {

    public static final String SAVING_SETTINGS_DIR = "RDC_base";
    public static final String LOG_FILE_NAME = "file_parse.log";

    public static void changeDbFolder(Stage stage) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if(selectedDirectory == null){
            //No Directory selected
        }else{
            Settings.getInstance().setBaseDir(selectedDirectory);
        }
    }

    public static File getLogFile() throws IOException {
        // получу папку для хранения файлов
        File saveDir = getSavingDir();
        if(saveDir != null){
            // верну файл
            return new File(saveDir, LOG_FILE_NAME);
        }
        return null;
    }

    public static File getSavingDir() throws IOException {
        // получу папку с настройками программ
        String savingDirPath = System.getenv("APPDATA");
        File savingDir = new File(savingDirPath);
        if(savingDir.isDirectory() && savingDir.canWrite()){
            // если системная папка доступна для записи
            // проверю, есть ли папка для приложения, если её нет- создам
            File propDir = new File(savingDir, SAVING_SETTINGS_DIR);
            if(!propDir.isDirectory()){
                if(!propDir.mkdir()){
                    throw new IOException("Не смог создать папку для хранения настроек");
                }
            }
            return propDir;
        }
        return null;
    }

    public static void changeTempFolder(Stage stage) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if(selectedDirectory == null){
            //No Directory selected
        }else{
            Settings.getInstance().setTempDir(selectedDirectory);
        }
    }
}
