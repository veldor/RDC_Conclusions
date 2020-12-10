package parser;

import models.handlers.StringsHandler;
import selections.Conclusion;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class InitialParser {

    private final Pattern sSignPattern;
    private final Pattern sShortPattern;
    String patternString = "^\\s*(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*(врач)?.+\\s([а-я]\\.)*\\s*([а-я]{5,})";
    String shortPatternString = "^\\s*(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*";
    LinesHandler mLinesHandler;
    Conclusion.Builder conclusionBuilder;
    boolean isDocFound = false;
    boolean isSexFound = false;
    boolean spendConclusion;
    boolean sKokuninFormat;
    static final String DOC_NOT_SELECTED = "Цветкова";
    private Matcher matcher;

    InitialParser() {
        mLinesHandler = new LinesHandler();
        sSignPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        sShortPattern = Pattern.compile(shortPatternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    class LinesHandler {

        private String mText;

        public void loadText(String text) {
            mText = text;
        }

        public void parse() throws ParseException {
            if (mText == null) {
                return;
            }
            // разобью текст по переносам строк
            String[] mStrings = mText.split("\n");
            for (String s : mStrings) {
                if (s.length() == 0) {
                    continue;
                }
                // разберу строку
                if (s.trim().startsWith(WordParser.PERSONALS_START)) {
                    // строка соответствует шаблону персональных данных
                    conclusionBuilder.setPersonals(StringsHandler.getStringFrom(s.trim(), WordParser.PERSONAL_PREFIX_LENGTH));
                }
                // разберу строку
                else if (s.trim().startsWith(WordParser.PERSONALS_START_1)) {
                    // строка соответствует шаблону персональных данных
                    conclusionBuilder.setPersonals(StringsHandler.getStringFrom(s.trim(), WordParser.PERSONAL_PREFIX_LENGTH_1));
                } else if (s.trim().startsWith(WordParser.BIRTH_DATE_START)) {
                    // строка соответствует шаблону даты рождения
                    conclusionBuilder.setBirthDate(StringsHandler.getStringFrom(s.trim(), WordParser.BIRTH_DATE_PREFIX_LENGTH));
                } else if (!isSexFound && s.trim().startsWith(WordParser.SEX_START)) {
                    conclusionBuilder.setSex(StringsHandler.getStringFrom(s.trim(), WordParser.SEX_PREFIX_LENGTH));
                } else if (s.trim().startsWith(WordParser.CONTRAST_START)) {
                    // строка соответствует шаблону контраста
                    conclusionBuilder.setContrast(StringsHandler.getStringFrom(s.trim(), WordParser.CONTRAST_PREFIX_LENGTH));
                } else if (s.trim().startsWith(WordParser.EXECUTION_AREA_START)) {
                    // строка соответствует шаблону области обследования
                    conclusionBuilder.setExecutionArea(StringsHandler.getStringFrom(s.trim(), WordParser.EXECUTION_AREA_PREFIX_LENGTH));
                } else if (s.trim().startsWith(WordParser.EXECUTION_NUMBER_START)) {
                    // строка соответствует шаблону номера обследования
                    conclusionBuilder.setExecutionNumber(StringsHandler.superTrim(StringsHandler.getStringFrom(s.trim(), WordParser.EXECUTION_NUMBER_PREFIX_LENGTH)));
                } else if (s.trim().startsWith(WordParser.EXECUTION_NUMBER_START_1)) {
                    // строка соответствует шаблону номера обследования
                    conclusionBuilder.setExecutionNumber(StringsHandler.superTrim(StringsHandler.getStringFrom(s.trim(), WordParser.EXECUTION_NUMBER_PREFIX_LENGTH_1)));
                } else if (s.trim().startsWith(WordParser.EXECUTION_DATE_START) && s.length() > WordParser.EXECUTION_DATE_PREFIX_LENGTH + 7) {
                    sKokuninFormat = true;
                    // строка соответствует шаблону номера обследования
                    conclusionBuilder.setExecutionDate(StringsHandler.getStringFrom(s.trim(), WordParser.EXECUTION_DATE_PREFIX_LENGTH));
                } else if (s.trim().length() > 10 && s.trim().substring(0, 10).toLowerCase().equals(WordParser.CONCLUSION_START)) {
                    // если дошли до заключения- отмечу, что оно найдено, для проверки на врача
                    spendConclusion = true;
                } else if (spendConclusion) {
                    if(sKokuninFormat && s.trim().startsWith(WordParser.KOKUNIN_DOCTOR_START)){
                        conclusionBuilder.setDiagnostician("Кокунин");
                    }
                    if(isDocFound){
                        matcher = sShortPattern.matcher(s.trim());
                        if (matcher.find()) {
                            // первая найденная группа- дата, тут всё просто, она уже отформатирована
                            conclusionBuilder.setExecutionDate(matcher.group(1));
                        }
                    }
                    else{
                        matcher = sSignPattern.matcher(s.trim());
                        if (matcher.find()) {
                            // первая найденная группа- дата, тут всё просто, она уже отформатирована
                            conclusionBuilder.setExecutionDate(matcher.group(1));
                            // с доктором чуть сложнее- нам нужна только фамилия
                            if (!isDocFound && !matcher.group(4).equals("Врач")) {
                                conclusionBuilder.setDiagnostician(matcher.group(4));
                            }
                        }
                    }
                }
            }
        }
    }
}
