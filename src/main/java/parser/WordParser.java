package parser;

import models.FilesUtils;
import models.handlers.FilesHandler;
import org.apache.poi.poifs.filesystem.FileMagic;
import selections.Conclusion;

import java.io.*;

public class WordParser {

    public static final String PERSONALS_START = "Фамилия, имя, отчество:";
    public static final String BIRTH_DATE_START = "Дата рождения:";
    public static final String SEX_START = "Пол:";
    public static final String EXECUTION_AREA_START = "Область исследования:";
    public static final String CONTRAST_START = "В/в контрастное усиление:";
    public static final String EXECUTION_NUMBER_START = "Номер исследования:";
    public static final String CONCLUSION_START = "заключение";

    public static final int PERSONAL_PREFIX_LENGTH = 23;
    public static final int BIRTH_DATE_PREFIX_LENGTH = 14;
    public static final int SEX_PREFIX_LENGTH = 4;
    public static final int EXECUTION_AREA_PREFIX_LENGTH = 21;
    public static final int CONTRAST_PREFIX_LENGTH = 25;
    public static final int EXECUTION_NUMBER_PREFIX_LENGTH = 19;

    private static WordParser instance;
    private FileWriter mLogWriter;

    private Conclusion mConclusion;
    private BufferedInputStream mInputStream;
    private FileMagic mFileMagicValue;

    public static WordParser getInstance() throws IOException {
        if(instance == null){
            instance = new WordParser();
        }
        return instance;
    }

    private WordParser() throws IOException {
        File logFile = FilesUtils.getLogFile();
        if(logFile != null){
            mLogWriter = new FileWriter(logFile, true);
        }
    }

    public Conclusion parse(File f) throws Exception {
        if(f.isFile() && f.length() > 0){
            mInputStream = new BufferedInputStream(new FileInputStream(f));
            mFileMagicValue = FileMagic.valueOf(mInputStream);
            if (mFileMagicValue == FileMagic.OLE2) {
                //it's DOC
                mConclusion = DocParser.getInstance().parse(f);
                return mConclusion;
            }
            else if(mFileMagicValue == FileMagic.OOXML){
                // it's DOCX
                mConclusion = DocxParser.getInstance().parse(f);
                return mConclusion;
            }
            else{
                // добавлю в лог запись о необработанном файле
                FilesHandler.addToLog("Пропущен файл: ", f, mLogWriter);
            }
        }
        return null;
    }
}
