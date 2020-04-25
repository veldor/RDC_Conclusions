package models.handlers;

import java.io.File;

public class GrammarHandler {
    public static String getDirValue(File dir) {
        if(dir == null){
            return "Папка не выбрана";
        }
        else{
            return dir.getAbsolutePath();
        }
    }
}
