package models.handlers;

import models.Settings;
import parser.WordParser;
import selections.Conclusion;

import java.io.File;
import java.util.ArrayList;

public class WorkConclusionsHandler {
    public static ArrayList<Conclusion> getConclusions() {
        ArrayList<Conclusion> results = new ArrayList<>();
        Conclusion conclusion;
        // получу список файлов в рабочей директории
        File dir = Settings.getInstance().getTempDir();
        if (dir.isDirectory()) {
            File[] content = dir.listFiles();
            if (content != null && content.length > 0) {
                for (File f :
                        content) {
                    // проверю, рабочий ли файл
                    String extension = FilesHandler.getExtension(f.getName());
                    // если файл подходит и ещё не в базе- добавлю его в список для конвертации
                    if (FilesHandler.accept(extension)) {
                        try {
                            conclusion = (new WordParser()).parse(f, true);
                            if (conclusion != null) {
                                results.add(conclusion);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return results;
    }
}
