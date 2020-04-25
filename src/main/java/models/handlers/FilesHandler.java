package models.handlers;

import models.FilesUtils;

import java.awt.*;
import java.io.*;
import java.util.Locale;

public class FilesHandler {

    /**
     * Проверю, парсится ли файл
     *
     * @param extension <p>Расширение файла</p>
     * @return <p>true- если файл в списке для парсинга, false- если нет</p>
     */
    public static boolean accept(String extension) {
        return extension.equals("doc") || extension.equals("docx");
    }

    /**
     * Добавлю значение в лог
     * @param message <p>Имя значения</p>
     * @param fileToQueue <p>Target file</p>
     * @param fileWriter <p>FileWriter instance</p>
     * @throws IOException <p>Exception</p>
     */
    public static void addToLog(String message, File fileToQueue, FileWriter fileWriter) throws IOException {
        fileWriter.append(String.format(Locale.ENGLISH, "%s %s\n", message, fileToQueue.getAbsolutePath()));
    }

    public static String getExtension(String name) {
        if (name != null) {
            return name.substring(name.lastIndexOf(".") + 1);
        }
        return null;
    }

    /**
     * Открывает лог в просмотрщике
     */
    public static void showLog() throws IOException {
        Desktop.getDesktop().open(FilesUtils.getLogFile());
    }
}
