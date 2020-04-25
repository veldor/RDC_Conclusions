package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    public static final String PROPERTIES_FILE_NAME = "properties";
    public static final String CONCLUSIONS_DIR_PATH = "conclusions_dir";
    private static Settings instance;
    private Properties mProperties;

    public static Settings getInstance(){
        if(instance == null){
            instance = new Settings();
        }
        return instance;
    }

    private Settings(){
        // инициализирую хранилище настроек
        try {
            File propertiesFile = getPropertiesFile();
            //Создаем объект свойст
            mProperties = new Properties();
            //Загружаем свойства из файла
            mProperties.load(new FileInputStream(propertiesFile));
            /*//Получаем в переменную значение конкретного свойства
            String host = properties.getProperty("host");
            //Устанавливаем значение свойста
            properties.setProperty("host", "localhost:8080");
            //Сохраняем свойства в файл.
            properties.store(new FileOutputStream(file), null);*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getPropertiesFile() throws IOException {
        File propertiesFile;
        File savingDir = FilesUtils.getSavingDir();
        propertiesFile = new File(savingDir, PROPERTIES_FILE_NAME);
        if(!propertiesFile.isFile()){
            // если файл настроек ещё не создан- создам его
            if(!propertiesFile.createNewFile()){
                throw new IOException("Не смог создать файл настроек");
            }
        }
        return propertiesFile;
    }

    public File getBaseDir() {
        String baseDirValue = mProperties.getProperty(CONCLUSIONS_DIR_PATH);
        if(baseDirValue != null && baseDirValue.length() > 0){
            File baseDir = new File(baseDirValue);
            if(baseDir.isDirectory()){
                return baseDir;
            }
        }
        return null;
    }

    public void setBaseDir(File selectedDirectory) throws IOException {
        if(selectedDirectory != null){
            mProperties.setProperty(CONCLUSIONS_DIR_PATH, selectedDirectory.getAbsolutePath());
            mProperties.store(new FileOutputStream(getPropertiesFile()), null);
        }
    }
}