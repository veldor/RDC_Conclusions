package parser;

import models.handlers.FilesHandler;
import org.apache.poi.poifs.filesystem.FileMagic;
import selections.Conclusion;

import java.io.*;

public class WordParser {

    public static final String PERSONALS_START = "Фамилия, имя, отчество";
    public static final String PERSONALS_START_1 = "Ф.И.О. пациента";
    public static final String BIRTH_DATE_START = "Дата рождения";
    public static final String SEX_START = "Пол:";
    public static final String EXECUTION_AREA_START = "Область исследования";
    public static final String CONTRAST_START = "В/в контрастное усиление";
    public static final String EXECUTION_NUMBER_START = "Номер исследования";
    public static final String EXECUTION_NUMBER_START_1 = "ID исследования";
    public static final String EXECUTION_DATE_START = "Дата исследования:";
    public static final String CONCLUSION_START = "заключение";
    public static final String KOKUNIN_DOCTOR_START = "Врач  ";

    public static final int PERSONAL_PREFIX_LENGTH = 23;
    public static final int PERSONAL_PREFIX_LENGTH_1 = 16;
    public static final int BIRTH_DATE_PREFIX_LENGTH = 14;
    public static final int SEX_PREFIX_LENGTH = 4;
    public static final int EXECUTION_AREA_PREFIX_LENGTH = 21;
    public static final int CONTRAST_PREFIX_LENGTH = 25;
    public static final int EXECUTION_NUMBER_PREFIX_LENGTH = 19;
    public static final int EXECUTION_NUMBER_PREFIX_LENGTH_1 = 16;
    public static final int EXECUTION_DATE_PREFIX_LENGTH = 19;

    private Conclusion conclusion;

    public Conclusion parse(File f, boolean skipWholenessCheck) throws Exception {
        if (f.isFile() && f.length() > 0) {
            String name = FilesHandler.getExtension(f.getName());
            if (name.equals("doc")) {
                //System.out.println("parse doc");
                //it's DOC
                //conclusion = (new DocParser()).parse(f, skipWholenessCheck);
                // конвертирую файл в docx и обработаю
            } else if (name.equals("docx")) {
                //System.out.println("parse docx");
                // it's DOCX
                conclusion = (new DocxParser()).parse(f, skipWholenessCheck);
            }
        }
        return conclusion;
    }
}
