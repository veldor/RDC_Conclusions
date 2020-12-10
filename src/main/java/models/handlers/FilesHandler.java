package models.handlers;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import db.Db;
import models.FilesUtils;
import models.Settings;
import selections.Conclusion;

import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.Locale;

public class FilesHandler {

    private static FileWriter sLogWriter;

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
     *
     * @param message     <p>Имя значения</p>
     * @param fileToQueue <p>Target file</p>
     */
    public static void addToLog(String message, String fileToQueue) {

        try {
            if (sLogWriter == null) {
                File logFile = FilesUtils.getLogFile();
                if (logFile != null) {
                    sLogWriter = new FileWriter(logFile, true);
                }
            }
            sLogWriter.append(String.format(Locale.ENGLISH, "%s %s\n", message, fileToQueue));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error write to log");
        }
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

    public static void openFile(String path) throws IOException {
        if (path != null) {
            openFile(new File(path));
        }
    }

    public static void openFile(File file) throws IOException {
        if (file.isFile() || file.isDirectory()) {
            Desktop.getDesktop().open(file);
        }
    }

    public static void openContainingFolder(String path) throws IOException {
        File file = new File(path);
        if (file.isFile()) {
            openContainingFolder(file);
        }
    }

    public static void openContainingFolder(File file) throws IOException {
        Runtime.getRuntime().exec(new String[]{
                "explorer.exe",
                "/select,",
                "\"" + file.getCanonicalPath() + "\""});
    }

    public static void useAsTemplate(String path) throws IOException {
        // получу исходный файл
        if (path != null && !path.isEmpty()) {
            File source = new File(path);
            if (source.isFile()) {
                File tempDir = Settings.getInstance().getTempDir();
                // Временное имя
                File newFile;
                // файл не должен существовать в папке до инициализации
                do {
                    String name = StringsHandler.getRandomString(50) + "." + StringsHandler.getExtension(path);
                    newFile = new File(tempDir, name);
                }
                while (newFile.isFile());
                copyFile(source, newFile);
                // открою скопированный файл для редактирования
                openFile(newFile);
                return;
            }
        }
        throw new IllegalArgumentException("Ошибка использования файла " + path + " как шаблона");
    }

    public static void copyFile(File source, File destination) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert is != null;
            is.close();
            assert os != null;
            os.close();
        }
    }

    public static File getPathToSave(Conclusion conclusion) throws SQLException {
        if (conclusion != null) {
            // получу область обследования. Проверю, зарегистрировано ли оно уже в базе, и существует ли папка
            // ,к которой оно привязано. Если всё нормально- верну путь к папке
            String savedPath = Db.getInstance().getExecutionAreaPath(conclusion.getExecutionArea());
            if (savedPath != null) {
                File dir = (new File(savedPath)).getParentFile();
                if (dir.isDirectory()) {
                    return dir;
                }
            }
        }
        return null;
    }

    public static File getUnhandledPath(File f) {
        File tempDir = new File("D:\\testBase\\unhandled");
        return new File(tempDir, f.getName());
    }

    public static File recreate(File f) {
        // создам новый файл рядом
        File newFile = new File(f.getPath().substring(0, f.getPath().length() - 4) + ".docx");
        try (InputStream docxInputStream = new FileInputStream(f); OutputStream outputStream = new FileOutputStream(newFile)) {
            IConverter converter = LocalConverter.builder().build();
            converter.convert(docxInputStream).as(DocumentType.MS_WORD).to(outputStream).as(DocumentType.DOCX).execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        f.delete();
        System.out.println("file " + newFile.getName() + " recreated");
        return newFile;
    }
}
